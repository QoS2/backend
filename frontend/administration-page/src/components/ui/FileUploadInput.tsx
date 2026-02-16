import { useState } from 'react';
import { uploadFile } from '../../api/upload';
import styles from './FileUploadInput.module.css';

interface FileUploadInputProps {
  label?: string;
  name?: string;
  value?: string;
  defaultValue?: string;
  accept?: string;
  type?: 'image' | 'audio';
  placeholder?: string;
  /** Controlled mode: 호출 시 URL 반영 */
  onChange?: (url: string) => void;
  /** 올린 파일 URL 취소(삭제) 버튼 표시 여부. 기본 true */
  showClearButton?: boolean;
  /** 취소 클릭 시 호출. S3 삭제 등. 호출 후 폼에서 URL 제거 */
  onClear?: (url: string) => void | Promise<void>;
}

export function FileUploadInput({
  label,
  name,
  value,
  defaultValue = '',
  accept,
  type = 'audio',
  placeholder = '파일을 선택하거나 URL을 입력하세요',
  onChange,
  showClearButton = true,
  onClear,
}: FileUploadInputProps) {
  const [internalValue, setInternalValue] = useState(defaultValue);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const isControlled = value !== undefined;
  const displayValue = isControlled ? value : internalValue;

  const setUrl = (url: string) => {
    if (!isControlled) setInternalValue(url);
    onChange?.(url);
  };

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setError(null);
    setUploading(true);
    try {
      const url = await uploadFile(file, type);
      setUrl(url);
    } catch (err) {
      setError(err instanceof Error ? err.message : '업로드 실패');
    } finally {
      setUploading(false);
      e.target.value = '';
    }
  };

  const acceptValue =
    accept ??
    (type === 'audio'
      ? 'audio/mpeg,audio/mp3,audio/wav,audio/ogg,audio/m4a,.mp3,.wav,.ogg,.m4a'
      : 'image/jpeg,image/png,image/gif,image/webp');

  const previewUrl = displayValue.trim();
  const hasPreview = previewUrl.length > 0 && !previewUrl.includes(' ');

  return (
    <div className={styles.wrapper}>
      {label && <label className={styles.label}>{label}</label>}
      <div className={styles.row}>
        <input
          type="text"
          name={name}
          value={displayValue}
          onChange={(e) => setUrl(e.target.value)}
          placeholder={placeholder}
          className={styles.urlInput}
        />
        <label className={styles.fileLabel}>
          <input
            type="file"
            accept={acceptValue}
            onChange={handleFileChange}
            disabled={uploading}
            className={styles.fileInput}
          />
          <span className={styles.fileButton}>
            {uploading ? '업로드 중…' : '파일 선택'}
          </span>
        </label>
      </div>
      {hasPreview && type === 'image' && (
        <div className={styles.preview}>
          <img src={previewUrl} alt="미리보기" className={styles.imagePreview} referrerPolicy="no-referrer" />
          {showClearButton && (
            <button
              type="button"
              className={styles.clearButton}
              onClick={async () => {
                if (displayValue.trim() && onClear) {
                  try {
                    await onClear(displayValue);
                  } catch (e) {
                    setError(e instanceof Error ? e.message : 'S3 삭제 실패');
                    return;
                  }
                }
                setUrl('');
              }}
              title="업로드 취소 (S3에서 삭제)"
            >
              ×
            </button>
          )}
        </div>
      )}
      {hasPreview && type === 'audio' && (
        <div className={styles.preview}>
          <audio controls src={previewUrl} className={styles.audioPreview} />
          {showClearButton && (
            <button
              type="button"
              className={styles.clearButton}
              onClick={async () => {
                if (displayValue.trim() && onClear) {
                  try {
                    await onClear(displayValue);
                  } catch (e) {
                    setError(e instanceof Error ? e.message : 'S3 삭제 실패');
                    return;
                  }
                }
                setUrl('');
              }}
              title="업로드 취소 (S3에서 삭제)"
            >
              ×
            </button>
          )}
        </div>
      )}
      {error && <p className={styles.error}>{error}</p>}
    </div>
  );
}
