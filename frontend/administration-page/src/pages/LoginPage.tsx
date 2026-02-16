import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { useAuth, AUTH_QUERY_KEY } from '../hooks/useAuth';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import {
  fetchLogin,
  fetchRegister,
  setAccessToken,
  type LoginRequest,
  type RegisterRequest,
} from '../api/auth';
import { OAUTH_GOOGLE_URL } from '../config/constants';
import styles from './LoginPage.module.css';

const FEATURES = [
  '투어/스팟/미션 정보 수정',
  '포토 제출 승인/거절 처리',
  '앱 연동 Enum 값 확인',
] as const;

export function LoginPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const queryClient = useQueryClient();
  const { data, isLoading, isError } = useAuth();

  const from = searchParams.get('from') ?? '/';
  const oauthError = searchParams.get('error') === 'oauth_failed';
  const oauthToken = searchParams.get('token');

  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [nickname, setNickname] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const handleJwtLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const body: LoginRequest = { email: email.trim(), password };
      await fetchLogin(body);
      await queryClient.invalidateQueries({ queryKey: AUTH_QUERY_KEY });
      navigate(from, { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : '로그인에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const body: RegisterRequest = {
        email: email.trim(),
        password,
        nickname: nickname.trim() || undefined,
      };
      await fetchRegister(body);
      await queryClient.invalidateQueries({ queryKey: AUTH_QUERY_KEY });
      navigate(from, { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : '회원가입에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleGoogleLogin = () => {
    window.location.href = OAUTH_GOOGLE_URL;
  };

  useEffect(() => {
    if (oauthToken) {
      setAccessToken(oauthToken);
      queryClient.invalidateQueries({ queryKey: AUTH_QUERY_KEY });
      navigate(from, { replace: true });
    }
  }, [oauthToken, from, queryClient, navigate]);

  if (oauthToken) {
    return null;
  }

  if (!isLoading && !isError && data) {
    return null;
  }

  return (
    <div className={styles.page}>
      <div className={styles.shell}>
        <section className={styles.hero}>
          <h1>투어 관리자 페이지</h1>
          <p className={styles.heroDesc}>
            투어를 설계하고, 당신의 아이디어를 서울 위에 펼쳐보세요.
          </p>

          <ul className={styles.featureList}>
            {FEATURES.map((text) => (
              <li key={text}>{text}</li>
            ))}
          </ul>
        </section>

        <section className={styles.card}>
          <h2>{mode === 'login' ? '로그인' : '회원가입'}</h2>
          <p className={styles.subtitle}>
            {mode === 'login'
              ? '등록된 관리자 계정으로 로그인하세요.'
              : '관리자 계정을 새로 생성합니다.'}
          </p>

          {mode === 'login' ? (
            <form onSubmit={handleJwtLogin} className={styles.form}>
              <Input
                type="email"
                label="이메일"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="admin@example.com"
                autoComplete="email"
                required
              />
              <Input
                type="password"
                label="비밀번호"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                autoComplete="current-password"
                required
              />
              {error && <p className={styles.error}>{error}</p>}

              <Button type="submit" variant="primary" className={styles.button} disabled={submitting}>
                {submitting ? '로그인 중…' : '로그인'}
              </Button>

              <button
                type="button"
                className={styles.linkButton}
                onClick={() => {
                  setMode('register');
                  setError(null);
                }}
              >
                계정이 없나요? 회원가입으로 이동
              </button>
            </form>
          ) : (
            <form onSubmit={handleRegister} className={styles.form}>
              <Input
                type="email"
                label="이메일"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="admin@example.com"
                autoComplete="email"
                required
              />
              <Input
                type="password"
                label="비밀번호 (8자 이상)"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                autoComplete="new-password"
                minLength={8}
                required
              />
              <Input
                type="text"
                label="닉네임 (선택)"
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                placeholder="닉네임"
                autoComplete="nickname"
              />
              {error && <p className={styles.error}>{error}</p>}

              <Button type="submit" variant="primary" className={styles.button} disabled={submitting}>
                {submitting ? '가입 중…' : '가입하기'}
              </Button>

              <button
                type="button"
                className={styles.linkButton}
                onClick={() => {
                  setMode('login');
                  setError(null);
                }}
              >
                로그인으로 돌아가기
              </button>
            </form>
          )}

          {mode === 'login' && (
            <>
              <div className={styles.divider}>
                <span>또는</span>
              </div>
              <Button type="button" variant="secondary" className={styles.button} onClick={handleGoogleLogin}>
                Google 로그인
              </Button>
            </>
          )}

          {oauthError && <p className={styles.error}>Google 로그인에 실패했습니다. 다시 시도해 주세요.</p>}
        </section>
      </div>
    </div>
  );
}
