// --- import ---
import { Routes, Route } from "react-router-dom";
import { ListPage } from "./pages/ListPage";
import { DetailPage } from "./pages/DetailPage";

function App() {
  return (
    // Routes 컴포넌트: 여러 Route들을 관리하는 컨테이너
    <Routes>
      {/* path="/"는 웹사이의 기본 경로 의미 */}
      {/* 이 경로로 접속하면 ListPage 컴포넌트를 띄워줌 */}
      <Route path="/" element={<ListPage />} />
      {/* path="/details/:id"는 동적 경로를 의미
      :id 부분에는 어떤 값이든 올 수 있음
      (예: /details/1, /details/abc).
      이런 형태의 경로로 접속하면 DetailPage 컴포넌트를 보여줌 */}
      <Route path="/details/:id" element={<DetailPage />} />
    </Routes>
  );
}

export default App;
