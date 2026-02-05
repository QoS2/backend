import { useEffect } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Drawer } from '../../../components/ui/Drawer';
import { Button } from '../../../components/ui/Button';
import { Input } from '../../../components/ui/Input';
import { Select } from '../../../components/ui/Select';
import { Switch } from '../../../components/ui/Switch';
import type { Quest } from '../../../types/admin';

const schema = z.object({
  title: z.string().min(1, '제목을 입력하세요'),
  subtitle: z.string().optional(),
  theme: z.string().min(1, '테마를 선택하세요'),
  tone: z.string().min(1, '톤을 선택하세요'),
  difficulty: z.string().min(1, '난이도를 선택하세요'),
  estimatedMinutes: z.coerce.number().int().min(0).optional().nullable(),
  startLocationLatitude: z.coerce.number().optional().nullable(),
  startLocationLongitude: z.coerce.number().optional().nullable(),
  isActive: z.boolean().optional(),
});

type FormValues = z.infer<typeof schema>;

interface QuestFormDrawerProps {
  open: boolean;
  onClose: () => void;
  quest: Quest | null;
  themeOptions: { value: string; label: string }[];
  toneOptions: { value: string; label: string }[];
  difficultyOptions: { value: string; label: string }[];
  onSubmit: (values: FormValues) => void;
  isSubmitting: boolean;
}

export function QuestFormDrawer({
  open,
  onClose,
  quest,
  themeOptions,
  toneOptions,
  difficultyOptions,
  onSubmit,
  isSubmitting,
}: QuestFormDrawerProps) {
  const isEdit = quest != null;

  const {
    register,
    control,
    handleSubmit,
    formState: { errors },
    setValue,
    watch,
    reset,
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      title: '',
      subtitle: '',
      theme: '',
      tone: '',
      difficulty: '',
      estimatedMinutes: null,
      startLocationLatitude: null,
      startLocationLongitude: null,
      isActive: true,
    },
  });

  const isActive = watch('isActive');

  useEffect(() => {
    if (!open) return;
    if (quest) {
      reset({
        title: quest.title,
        subtitle: quest.subtitle ?? '',
        theme: quest.theme,
        tone: quest.tone,
        difficulty: quest.difficulty,
        estimatedMinutes: quest.estimatedMinutes ?? null,
        startLocationLatitude: quest.startLocationLatitude ?? null,
        startLocationLongitude: quest.startLocationLongitude ?? null,
        isActive: quest.isActive,
      });
    } else {
      reset({
        title: '',
        subtitle: '',
        theme: '',
        tone: '',
        difficulty: '',
        estimatedMinutes: null,
        startLocationLatitude: null,
        startLocationLongitude: null,
        isActive: true,
      });
    }
  }, [open, quest, reset]);

  const footer = (
    <>
      <Button variant="ghost" onClick={onClose}>
        취소
      </Button>
      <Button
        variant="primary"
        onClick={handleSubmit(onSubmit)}
        disabled={isSubmitting}
      >
        {isSubmitting ? '저장 중…' : isEdit ? '수정' : '생성'}
      </Button>
    </>
  );

  return (
    <Drawer
      open={open}
      onClose={onClose}
      title={isEdit ? '퀘스트 수정' : '퀘스트 생성'}
      footer={footer}
    >
      <form
        onSubmit={handleSubmit(onSubmit)}
        style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-md)' }}
      >
        <Input
          label="제목"
          {...register('title')}
          error={errors.title?.message}
        />
        <Input label="부제목" {...register('subtitle')} />
        <Controller
          name="theme"
          control={control}
          render={({ field }) => (
            <Select
              label="테마"
              options={themeOptions}
              value={field.value}
              onChange={(e) => field.onChange(e.target.value)}
              onBlur={field.onBlur}
              error={errors.theme?.message}
            />
          )}
        />
        <Controller
          name="tone"
          control={control}
          render={({ field }) => (
            <Select
              label="톤"
              options={toneOptions}
              value={field.value}
              onChange={(e) => field.onChange(e.target.value)}
              onBlur={field.onBlur}
              error={errors.tone?.message}
            />
          )}
        />
        <Controller
          name="difficulty"
          control={control}
          render={({ field }) => (
            <Select
              label="난이도"
              options={difficultyOptions}
              value={field.value}
              onChange={(e) => field.onChange(e.target.value)}
              onBlur={field.onBlur}
              error={errors.difficulty?.message}
            />
          )}
        />
        <Input
          label="예상 소요 시간 (분)"
          type="number"
          {...register('estimatedMinutes')}
          error={errors.estimatedMinutes?.message}
        />
        <Input
          label="시작 위치 위도"
          type="number"
          step="any"
          {...register('startLocationLatitude')}
        />
        <Input
          label="시작 위치 경도"
          type="number"
          step="any"
          {...register('startLocationLongitude')}
        />
        {isEdit && (
          <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--space-sm)' }}>
            <span className="label">활성</span>
            <Switch
              checked={isActive ?? true}
              onChange={(v) => setValue('isActive', v)}
              aria-label="활성 여부"
            />
          </div>
        )}
      </form>
    </Drawer>
  );
}
