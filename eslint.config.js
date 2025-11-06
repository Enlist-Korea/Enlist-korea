// import js from '@eslint/js';
// import globals from 'globals';
// import reactHooks from 'eslint-plugin-react-hooks';
// import reactRefresh from 'eslint-plugin-react-refresh';
// import { defineConfig, globalIgnores } from 'eslint/config';

// export default defineConfig([
//   globalIgnores(['dist']),
//   {
//     files: ['**/*.{js,jsx}'],
//     extends: [
//       js.configs.recommended,
//       reactHooks.configs['recommended-latest'],
//       reactRefresh.configs.vite,
//     ],
//     languageOptions: {
//       ecmaVersion: 2020,
//       globals: globals.browser,
//       parserOptions: {
//         ecmaVersion: 'latest',
//         ecmaFeatures: { jsx: true },
//         sourceType: 'module',
//       },
//     },
//     rules: {
//       'no-unused-vars': ['error', { varsIgnorePattern: '^[A-Z_]' }],
//       'no-var': 'error',
//     },
//   },
// ]);
import js from "@eslint/js";
import globals from "globals";
import { FlatCompat } from "@eslint/eslintrc";
import path from "path";
import { fileURLToPath } from "url";
import reactHooks from "eslint-plugin-react-hooks";
import reactRefresh from "eslint-plugin-react-refresh";

// 새로 설치한 Prettier 충돌 방지 설정
import configPrettier from "eslint-config-prettier";

// FlatCompat을 위한 경로 설정
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const compat = new FlatCompat({
  baseDirectory: __dirname,
  resolvePluginsRelativeTo: __dirname,
});

/** @type {import('eslint').Linter.FlatConfig[]} */
export default [
  // 1. 전역 ignore 설정 (기존과 동일)
  {
    ignores: ["dist/**"],
  },

  // 2. Eslint 기본 규칙
  js.configs.recommended,

  // 3. Airbnb 규칙 적용 (compat.extends 사용)
  // 'airbnb'는 React를 포함합니다. (React가 없다면 'airbnb-base' 사용)
  ...compat.extends("airbnb"),

  // 4. React Hooks 및 Refresh 규칙 (기존과 동일)
  reactHooks.configs["recommended-latest"],
  reactRefresh.configs.vite, // Vite 환경이므로 그대로 둠

  // 5. 프로젝트 전반에 적용할 언어/환경 설정
  {
    files: ["**/*.{js,jsx}"], // js와 jsx 파일에 모두 적용
    languageOptions: {
      ecmaVersion: 2020, // 기존 설정 유지
      sourceType: "module",
      globals: {
        ...globals.browser, // 브라우저 환경
      },
      parserOptions: {
        ecmaFeatures: {
          jsx: true, // JSX 사용
        },
      },
    },
    rules: {
      // 6. 사용자 정의 규칙 (기존 설정 유지)
      "no-unused-vars": ["error", { varsIgnorePattern: "^[A-Z_]" }],
      "no-var": "error",

      // React 17+ 및 Vite 환경에서는 이 규칙이 필요X
      "react/react-in-jsx-scope": "off",
      "react/jsx-uses-react": "off",

      // import/extensions 규칙은 .js, .jsx를 생략할 수 있도록 설정
      "import/extensions": [
        "error",
        "ignorePackages",
        {
          js: "never",
          jsx: "never",
        },
      ],
    },
  },

  // 7. Prettier 설정
  // Prettier와 충돌하는 ESLint 규칙들을 비활성화
  configPrettier,
];
