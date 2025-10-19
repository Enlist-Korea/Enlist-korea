import {
  useParams,
  useSearchParams,
} from "react-router-dom";
import { useState, useEffect } from "react";
import mmaData from "../data/mmaData.json"; // ✅ 로컬 JSON 불러오기

export default function DetailPage() {
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const name = searchParams.get("name") || "특기 정보";

  const [html, setHtml] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);

    // 로컬 JSON에서 id 일치하는 데이터 찾기
    const specialty = mmaData.find(
      (item) => item.id === id,
    );

    if (specialty) {
      setHtml(specialty.html);
    } else {
      setHtml(
        `<p style="color:red;"> ID ${id} 에 해당하는 캐시된 데이터가 없습니다.</p>`,
      );
    }

    setLoading(false);
  }, [id]);

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
