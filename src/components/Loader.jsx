import styles from "../css/Loader.module.css";

// 데이터를 불러오는 동안 보여줄 로딩 Spinner 컴포넌트

export const Loader = () => {
  return (
    <div className="loader-wrapper">
      <div className={styles.loader}></div>
    </div>
  );
};
