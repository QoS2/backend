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
      {error && <p className={styles.error}>{error}</p>}
    </div>
  );
}
