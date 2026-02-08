import { useRef, useState } from 'react';
import { uploadFile } from '../../api/upload';
import styles from './FileUploadInput.module.css';

interface FileUploadInputProps {
  label: string;
  name: string;
  defaultValue?: string;
  accept?: string;
  type?: 'image' | 'audio';
  placeholder?: string;
}

export function FileUploadInput({
  label,
  name,
  defaultValue = '',
  accept,
  type = 'audio',
  placeholder = '파일을 선택하거나 URL을 입력하세요',
}: FileUploadInputProps) {
  const urlInputRef = useRef<HTMLInputElement>(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setError(null);
    setUploading(true);
    try {
      const url = await uploadFile(file, type);
      if (urlInputRef.current) {
        urlInputRef.current.value = url;
      }
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
      <label className={styles.label}>{label}</label>
      <div className={styles.row}>
        <input
          ref={urlInputRef}
          type="text"
          name={name}
          defaultValue={defaultValue}
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
