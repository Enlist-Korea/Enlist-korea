import styles from '../css/Loader.module.css';

// 데이터를 불러오는 동안 보여줄 로딩 Spinner 컴포넌트

const loadingSpinner = () => {
  <div className="loader-wrapper">
    <div className={styles.loader} />
  </div>;
};

export default loadingSpinner;
