/**
 * 병무청 군별 자세한 군사특기임무 및 설명 링크
 * 특기 번호와 이름을 key-value로 정의
 
 * 육군: 기술행정병, 기술행정병(연단위), 전문특기병
 * 해군: 기술병
 * 해병대: 기술병, 전문특기병
 * 공군: 기술병, 전문특기병
 */

export const SPECIALITY_LINKS = {
  육군: {
    기술행정병: {
      121.101: {
        MOS_NAME: "K계열전차승무",
        params: {
          specialityCode: "121.101",
          gunCode: "1",
          etc: "1",
        },
      },
    },
  },
};
