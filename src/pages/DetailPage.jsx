import {
  useParams,
  useSearchParams,
} from "react-router-dom";
import { useState, useEffect } from "react";

// 1. 우리가 만든 JSON 파일을 직접 import
import armyData from "../const/armyData.json";

export default function DetailPage() {
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const name = searchParams.get("name") || "특기 정보";

  const [html, setHtml] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);

    // 2. armyData 배열에서 URL의 id와 일치하는 항목을 찾음
    const specialty = armyData.find(
      (item) => item.id === id,
    );

    if (specialty) {
      // 3. 찾았으면 그 항목의 detailHtml 값을 html 상태로 설정
      setHtml(specialty.detailHtml);
    } else {
      // 못 찾았으면 에러 메시지 설정
      setHtml(
        `<p style="color:red;">ID ${id}에 해당하는 정보를 찾을 수 없습니다.</p>`,
      );
    }

    setLoading(false);
  }, [id]); // id가 바뀔 때마다 이 로직을 다시 실행

  return (
    <div style={{ padding: "2rem" }}>
      <h1
        style={{ fontSize: "1.5rem", marginBottom: "1rem" }}
      >
        {name} ({id})
      </h1>
      {loading ? (
        <p>데이터를 불러오는 중입니다...</p>
      ) : (
        <div
          style={{
            background: "#fff",
            color: "#000",
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
