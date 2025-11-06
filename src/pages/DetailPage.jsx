import {
  useParams,
  useSearchParams,
} from "react-router-dom";
import { useState, useEffect } from "react";
import mmaData from "../data/mmaData.json"; // 로컬 JSON 불러오기

/**
 * 목록 페이지(ListPage)에서 '자세히 보기'를 클릭하면 이동하는 상세 페이지
 * URL에 포함된 'id' 값을 사용해 `mmaData.json`에서 일치하는 특기 정보를 탐색
 * URL의 쿼리 파라미터에서 `name` 값을 가져와 제목으로 표시
 * 찾은 데이터의 `html` 내용을 화면에 렌더링
 */
export default function DetailPage() {
  // useParams() 메서드로 URL 경로에서 :id 값을 추출 => id 값이 특기 번호
  const { id } = useParams();

  // useSearchParams() 메서드로 URL의 쿼리 문자열 값을 가져옴
  const [searchParams] = useSearchParams();

  // /details/133.106?name=포병레이더 => name은 "포병레이더"
  // 'name' 값이 없으면 "특기 정보"를 기본값으로 사용
  const name = searchParams.get("name") || "특기 정보";

  // --- 상태 관리(State) ---
  const [html, setHtmlState] = useState("");
  const [loading, setLoadingState] = useState(true);

  useEffect(() => {
    setLoadingState(true);

    // mmaData에서 URL의 id와 일치하는 항목을 검색
    const specialtyInfo = mmaData.find((MOS) => MOS.id === id);

    // 일치하는 데이터 찾았는지 확인
    if (specialtyInfo) {
      // 찾은 경우: 해당 데이터의 html 내용을 상태에 저장
      setHtmlState(specialtyInfo.html);
    } else {
      // 없으면: 사용자에게 에러 메시지 띄움
      setHtmlState(
        `<p style="color:red;"> ID ${id} 에 해당하는 캐시된 데이터가 없습니다.</p>`,
      );
    }

    setLoadingState(false); // 데이터 검색 완료 시 로딩 상태 비활성화
  }, [id]); // `id` 값이 바뀔 때만 재실행

  // --- 렌더링 ---
  return (
    <div style={{ padding: "2rem" }}>
      <h1
        style={{ fontSize: "1.5rem", marginBottom: "1rem" }}
      >
        {name} ({id})
      </h1>

      {loading ? (
        <p>불러오는 중...</p>
      ) : (
        <div
          style={{
            background: "#121212",
            color: "#FFFFFF",
            borderRadius: "8px",
            padding: "1.5rem",
            overflowX: "auto",
          }}
          dangerouslySetInnerHTML={{ __html: html }}
        />
      )}
    </div>
  );
}
