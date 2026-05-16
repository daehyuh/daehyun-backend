package com.example.daehyunbackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "tribunal_case_cafe_link",
        indexes = {
                @Index(name = "idx_tribunal_case_cafe_link_case", columnList = "case_id")
        }
)
public class TribunalCaseCafeLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private TribunalCase tribunalCase;

    @Column(name = "url", nullable = false, length = 1000)
    private String url;

    public static TribunalCaseCafeLink create(TribunalCase tribunalCase, String url) {
        TribunalCaseCafeLink link = new TribunalCaseCafeLink();
        link.tribunalCase = tribunalCase;
        link.url = url;
        return link;
    }
}
