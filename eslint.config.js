import js from '@eslint/js';
import globals from 'globals';
import { FlatCompat } from '@eslint/eslintrc';
import path from 'path';
import { fileURLToPath } from 'url';

// 플러그인 임포트
import reactRefresh from 'eslint-plugin-react-refresh';

// Prettier 충돌 방지 설정
import configPrettier from 'eslint-config-prettier';

// FlatCompat을 위한 경로 설정
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const compat = new FlatCompat({
  baseDirectory: __dirname,
  resolvePluginsRelativeTo: __dirname,
});

/** @type {import('eslint').Linter.FlatConfig[]} */
export default [
  // 1. 전역 ignore 설정
  {
    ignores: ['dist/**'],
  },

  // 2. Eslint 기본 규칙
  js.configs.recommended,

  // 3. Airbnb 규칙 적용
  // (여기에 이미 react-hooks 규칙이 포함)
  ...compat.extends('airbnb'),

  // 4. React Refresh 규칙
  reactRefresh.configs.vite,

  // 5. 프로젝트 전반에 적용할 언어/환경 설정
  {
    files: ['**/*.{js,jsx}'], // js와 jsx 파일에 모두 적용
    languageOptions: {
      ecmaVersion: 2020,
      sourceType: 'module',
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
      // 6. 사용자 정의 규칙
      'no-unused-vars': ['error', { varsIgnorePattern: '^[A-Z_]' }],
      'no-var': 'error',

      // React 17+ 및 Vite 환경에서는 이 규칙이 필요없음
      'react/react-in-jsx-scope': 'off',
      'react/jsx-uses-react': 'off',

      // import/extensions 규칙은 .js, .jsx를 생략할 수 있도록 설정
      'import/extensions': [
        'error',
        'ignorePackages',
        {
          js: 'never',
          jsx: 'never',
        },
      ],
      // Airbnb 규칙 중 일부를 편의상 비활성화 (선택 사항)
      'react/prop-types': 'off', // prop-types 사용을 강제하지 않음
      'react/jsx-props-no-spreading': 'off', // props spreading 허용
    },
  },

  // 7. Prettier 설정
  configPrettier,
];
