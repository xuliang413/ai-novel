package net.lab1024.sa.admin.module.business.novel.domain.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * M1 写前意图。
 */
@Data
public class ChapterIntentModel {

    private Long projectId;

    private Integer chapterNo;

    private String pov;

    private String chapterGoal;

    private List<ChapterIntentCandidateModel> candidateCharacters = new ArrayList<>();

    private List<ChapterIntentCandidateModel> targetClues = new ArrayList<>();

    private List<ChapterIntentCandidateModel> candidateLocations = new ArrayList<>();

    private List<String> extraInstructions = new ArrayList<>();
}
