package com.example.demo.TEST_001.dto;

import lombok.Data;

/**
 * 식품안전나라 레시피 API 응답 데이터 매핑 클래스
 */
@Data
public class RecipeDTO {
    // 기본 정보
    private String rcpSeq;              // 일련번호
    private String rcpNm;               // 메뉴명
    private String rcpWay2;             // 조리방법
    private String rcpPat2;             // 요리종류

    // 영양정보
    private String infoWgt;             // 중량(1인분)
    private String infoEng;             // 열량
    private String infoCar;             // 탄수화물
    private String infoPro;             // 단백질
    private String infoFat;             // 지방
    private String infoNa;              // 나트륨

    // 재료 및 조리법
    private String rcpPartsDtls;        // 재료정보

    // 조리 단계 설명 (최대 20단계)
    private String manual01;
    private String manual02;
    private String manual03;
    private String manual04;
    private String manual05;
    private String manual06;
    private String manual07;
    private String manual08;
    private String manual09;
    private String manual10;
    private String manual11;
    private String manual12;
    private String manual13;
    private String manual14;
    private String manual15;
    private String manual16;
    private String manual17;
    private String manual18;
    private String manual19;
    private String manual20;

    // 조리 단계 이미지 (최대 20단계)
    private String manualImg01;
    private String manualImg02;
    private String manualImg03;
    private String manualImg04;
    private String manualImg05;
    private String manualImg06;
    private String manualImg07;
    private String manualImg08;
    private String manualImg09;
    private String manualImg10;
    private String manualImg11;
    private String manualImg12;
    private String manualImg13;
    private String manualImg14;
    private String manualImg15;
    private String manualImg16;
    private String manualImg17;
    private String manualImg18;
    private String manualImg19;
    private String manualImg20;

    // 이미지
    private String attFileNoMain;       // 이미지경로(소)
    private String attFileNoMk;         // 이미지경로(대)

    // 기타
    private String rcpNaTip;            // 저감 조리법 TIP
    private String hashTag;             // 해쉬태그

    // 매칭 관련 필드 (계산용)
    private int matchedIngredientCount;  // 매칭된 재료 개수
    private int totalIngredientCount;    // 전체 재료 개수
    private double matchScore;           // 매칭 점수
    private String matchedIngredients;   // 매칭된 재료 목록 (콤마 구분)
}
