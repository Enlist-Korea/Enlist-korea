// --- import ---
import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import App from "./App.jsx";
import "../src/css/global.css";

// public/index.html에 있는 파일에 있는 id가 "root"인 DOM 요소를 찾아서 리액트 앱의 시작점으로 지정
ReactDOM.createRoot(document.getElementById("root")).render(
  // 개발 중에 발생하는 잠재적인 문제를 감지하고 경고를 표시
  <React.StrictMode>
    {/*
     * BrowerRouter로 App 컴포넌트를 감싸줌
     * 이 과정으로 App 컴포넌트와 자식 컴포넌트들 안에서 URL에 따라 다른 페이지를 보여주는 라우팅 기능    사용 가능
     */}
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </React.StrictMode>,
);
