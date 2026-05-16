package com.example.daehyunbackend.service;

import com.example.daehyunbackend.dto.TribunalCaseCreateRequest;
import com.example.daehyunbackend.dto.TribunalCommentRequest;
import com.example.daehyunbackend.dto.TribunalVoteRequest;
import com.example.daehyunbackend.entity.Role;
import com.example.daehyunbackend.entity.TribunalCase;
import com.example.daehyunbackend.entity.TribunalCaseCafeLink;
import com.example.daehyunbackend.entity.TribunalCaseComment;
import com.example.daehyunbackend.entity.TribunalCaseVote;
import com.example.daehyunbackend.entity.TribunalCaseView;
import com.example.daehyunbackend.entity.TribunalCommentLike;
import com.example.daehyunbackend.entity.TribunalReplayMessage;
import com.example.daehyunbackend.entity.TribunalVerdict;
import com.example.daehyunbackend.entity.User;
import com.example.daehyunbackend.repository.TribunalCaseCafeLinkRepository;
import com.example.daehyunbackend.repository.TribunalCaseCommentRepository;
import com.example.daehyunbackend.repository.TribunalCaseRepository;
import com.example.daehyunbackend.repository.TribunalCaseVoteRepository;
import com.example.daehyunbackend.repository.TribunalCaseViewRepository;
import com.example.daehyunbackend.repository.TribunalCommentLikeRepository;
import com.example.daehyunbackend.repository.TribunalReplayMessageRepository;
import com.example.daehyunbackend.response.TribunalCafeLinkResponse;
import com.example.daehyunbackend.response.TribunalCaseDetailResponse;
import com.example.daehyunbackend.response.TribunalCaseSummaryResponse;
import com.example.daehyunbackend.response.TribunalCommentResponse;
import com.example.daehyunbackend.response.TribunalPageResponse;
import com.example.daehyunbackend.response.TribunalReplayMessageResponse;
import com.example.daehyunbackend.response.TribunalVoteSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TribunalService {
    private final TribunalReplayScraper replayScraper;
    private final TribunalCaseRepository tribunalCaseRepository;
    private final TribunalCaseCafeLinkRepository cafeLinkRepository;
    private final TribunalReplayMessageRepository replayMessageRepository;
    private final TribunalCaseVoteRepository voteRepository;
    private final TribunalCaseViewRepository viewRepository;
    private final TribunalCaseCommentRepository commentRepository;
    private final TribunalCommentLikeRepository commentLikeRepository;

    @Transactional
    public TribunalCaseDetailResponse createCase(TribunalCaseCreateRequest request, User author) {
        validateCreateRequest(request);

        TribunalReplayScraper.ReplayParseResult replay = replayScraper.scrape(request.replayUrl());
        LocalDateTime now = LocalDateTime.now();
        TribunalCase tribunalCase = tribunalCaseRepository.save(TribunalCase.create(
                author,
                request.replayUrl().trim(),
                replay.roomId(),
                replay.lang(),
                request.nickname().trim(),
                request.pick().trim(),
                normalizeOptional(request.description()),
                replay.winnerTeam(),
                replay.gameType(),
                replay.gameDuration(),
                now
        ));

        saveCafeLinks(tribunalCase, request.cafeLinks());
        saveReplayMessages(tribunalCase, replay.messages());

        return getCase(tribunalCase.getId(), author);
    }

    @Transactional(readOnly = true)
    public TribunalPageResponse<TribunalCaseSummaryResponse> getCases(Integer page, Integer size, User viewer) {
        int normalizedPage = Math.max(page == null ? 0 : page, 0);
        int normalizedSize = Math.max(1, Math.min(size == null ? 20 : size, 100));

        Page<TribunalCase> casePage = tribunalCaseRepository.findAllByOrderByCreatedAtDesc(
                PageRequest.of(normalizedPage, normalizedSize)
        );

        List<TribunalCaseSummaryResponse> content = casePage.getContent().stream()
                .map(tribunalCase -> TribunalCaseSummaryResponse.from(
                        tribunalCase,
                        voteSummary(tribunalCase, viewer),
                        viewRepository.countByTribunalCase(tribunalCase),
                        commentRepository.countByTribunalCaseAndDeletedFalse(tribunalCase)
                ))
                .toList();

        return new TribunalPageResponse<>(
                normalizedPage,
                normalizedSize,
                casePage.getTotalElements(),
                casePage.getTotalPages(),
                content
        );
    }

    @Transactional
    public TribunalCaseDetailResponse getCase(Long caseId, User viewer) {
        TribunalCase tribunalCase = findCase(caseId);
        recordView(tribunalCase, viewer);
        return TribunalCaseDetailResponse.from(
                tribunalCase,
                cafeLinkRepository.findByTribunalCaseOrderByIdAsc(tribunalCase).stream()
                        .map(TribunalCafeLinkResponse::from)
                        .toList(),
                replayMessageRepository.findByTribunalCaseOrderBySequenceNoAsc(tribunalCase).stream()
                        .map(TribunalReplayMessageResponse::from)
                        .toList(),
                voteSummary(tribunalCase, viewer),
                viewRepository.countByTribunalCase(tribunalCase),
                commentRepository.findByTribunalCaseOrderByCreatedAtAsc(tribunalCase).stream()
                        .map(comment -> commentResponse(comment, viewer))
                        .toList()
        );
    }

    @Transactional
    public TribunalVoteSummaryResponse vote(Long caseId, TribunalVoteRequest request, User voter) {
        if (request == null || request.verdict() == null) {
            throw new IllegalArgumentException("투표 값을 입력해주세요.");
        }

        TribunalCase tribunalCase = findCase(caseId);
        LocalDateTime now = LocalDateTime.now();
        TribunalCaseVote vote = voteRepository.findByTribunalCaseAndVoter(tribunalCase, voter)
                .orElseGet(() -> TribunalCaseVote.create(tribunalCase, voter, request.verdict(), now));
        vote.setVerdict(request.verdict());
        vote.setUpdatedAt(now);
        voteRepository.save(vote);

        return voteSummary(tribunalCase, voter);
    }

    @Transactional
    public TribunalCommentResponse addComment(Long caseId, TribunalCommentRequest request, User author) {
        String content = normalizeRequired(request == null ? null : request.content(), "댓글 내용을 입력해주세요.");
        TribunalCase tribunalCase = findCase(caseId);
        TribunalCaseComment parent = null;
        Long parentId = request == null ? null : request.parentId();

        if (parentId != null) {
            parent = findComment(parentId);
            if (!parent.getTribunalCase().getId().equals(tribunalCase.getId())) {
                throw new IllegalArgumentException("다른 사건의 댓글에는 답글을 작성할 수 없습니다.");
            }
            if (parent.getParent() != null) {
                throw new IllegalArgumentException("대댓글에는 답글을 작성할 수 없습니다.");
            }
        }

        TribunalCaseComment comment = commentRepository.save(TribunalCaseComment.create(
                tribunalCase,
                author,
                parent,
                content,
                LocalDateTime.now()
        ));
        return commentResponse(comment, author);
    }

    @Transactional
    public TribunalCommentResponse updateComment(Long commentId, TribunalCommentRequest request, User user) {
        TribunalCaseComment comment = findComment(commentId);
        requireCommentOwnerOrAdmin(comment, user);
        if (comment.isDeleted()) {
            throw new IllegalArgumentException("삭제된 댓글은 수정할 수 없습니다.");
        }

        comment.setContent(normalizeRequired(request == null ? null : request.content(), "댓글 내용을 입력해주세요."));
        comment.setUpdatedAt(LocalDateTime.now());
        return commentResponse(comment, user);
    }

    @Transactional
    public void deleteComment(Long commentId, User user) {
        TribunalCaseComment comment = findComment(commentId);
        requireCommentOwnerOrAdmin(comment, user);
        comment.setDeleted(true);
        comment.setContent("");
        comment.setUpdatedAt(LocalDateTime.now());
    }

    @Transactional
    public TribunalCommentResponse toggleCommentLike(Long commentId, User user) {
        TribunalCaseComment comment = findComment(commentId);
        if (comment.isDeleted()) {
            throw new IllegalArgumentException("삭제된 댓글에는 인정할 수 없습니다.");
        }

        commentLikeRepository.findByCommentAndUser(comment, user)
                .ifPresentOrElse(
                        commentLikeRepository::delete,
                        () -> commentLikeRepository.save(TribunalCommentLike.create(comment, user, LocalDateTime.now()))
                );
        return commentResponse(comment, user);
    }

    private void validateCreateRequest(TribunalCaseCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("사건 정보를 입력해주세요.");
        }
        normalizeRequired(request.replayUrl(), "리플레이 링크를 입력해주세요.");
        normalizeRequired(request.nickname(), "닉네임을 입력해주세요.");
        normalizeRequired(request.pick(), "픽을 입력해주세요.");
    }

    private void saveCafeLinks(TribunalCase tribunalCase, List<String> cafeLinks) {
        if (cafeLinks == null || cafeLinks.isEmpty()) {
            return;
        }

        Set<String> uniqueLinks = new LinkedHashSet<>();
        for (String link : cafeLinks) {
            String normalized = normalizeOptional(link);
            if (normalized != null) {
                uniqueLinks.add(normalized);
            }
        }

        List<TribunalCaseCafeLink> links = uniqueLinks.stream()
                .map(link -> TribunalCaseCafeLink.create(tribunalCase, link))
                .toList();
        cafeLinkRepository.saveAll(links);
    }

    private void saveReplayMessages(
            TribunalCase tribunalCase,
            List<TribunalReplayScraper.ReplayMessageData> replayMessages
    ) {
        List<TribunalReplayMessage> messages = replayMessages.stream()
                .map(message -> TribunalReplayMessage.create(
                        tribunalCase,
                        message.sequenceNo(),
                        message.messageType(),
                        message.chatType(),
                        message.nickname(),
                        message.jobCode(),
                        message.jobImageUrl(),
                        message.frameImageUrl(),
                        message.content()
                ))
                .toList();
        replayMessageRepository.saveAll(messages);
    }

    private TribunalVoteSummaryResponse voteSummary(TribunalCase tribunalCase, User viewer) {
        TribunalVerdict myVerdict = viewer == null ? null : voteRepository.findByTribunalCaseAndVoter(tribunalCase, viewer)
                .map(TribunalCaseVote::getVerdict)
                .orElse(null);
        return new TribunalVoteSummaryResponse(
                voteRepository.countByTribunalCaseAndVerdict(tribunalCase, TribunalVerdict.GUILTY),
                voteRepository.countByTribunalCaseAndVerdict(tribunalCase, TribunalVerdict.NOT_GUILTY),
                myVerdict
        );
    }

    private void recordView(TribunalCase tribunalCase, User viewer) {
        if (viewer == null) {
            return;
        }

        viewRepository.findByTribunalCaseAndUser(tribunalCase, viewer)
                .orElseGet(() -> viewRepository.save(TribunalCaseView.create(
                        tribunalCase,
                        viewer,
                        LocalDateTime.now()
                )));
    }

    private TribunalCommentResponse commentResponse(TribunalCaseComment comment, User viewer) {
        boolean likedByMe = viewer != null && commentLikeRepository.findByCommentAndUser(comment, viewer).isPresent();
        return TribunalCommentResponse.from(
                comment,
                commentLikeRepository.countByComment(comment),
                likedByMe
        );
    }

    private TribunalCase findCase(Long caseId) {
        return tribunalCaseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("사건을 찾을 수 없습니다."));
    }

    private TribunalCaseComment findComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
    }

    private void requireCommentOwnerOrAdmin(TribunalCaseComment comment, User user) {
        boolean owner = comment.getAuthor().getId().equals(user.getId());
        boolean admin = user.getRole() == Role.ROLE_ADMIN;
        if (!owner && !admin) {
            throw new AccessDeniedException("댓글을 수정할 권한이 없습니다.");
        }
    }

    private String normalizeRequired(String value, String message) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
