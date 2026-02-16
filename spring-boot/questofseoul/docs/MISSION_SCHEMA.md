# Mission options_json / answer_json 스키마

## 목적

미션에서 **문제( prompt ), 보기( options ), 이미지**를 지원합니다.

---

## options_json 권장 구조

### QUIZ (객관식)

```json
{
  "choices": [
    { "id": "a", "text": "보기1 텍스트", "imageUrl": "https://s3.../mission/choice_a.png" },
    { "id": "b", "text": "보기2 텍스트" },
    { "id": "c", "text": "보기3 텍스트", "imageUrl": "https://s3.../mission/choice_c.png" }
  ],
  "questionImageUrl": "https://s3.../mission/question.png"
}
```

- `choices`: 보기 배열. `id`, `text` 필수. `imageUrl` 선택.
- `questionImageUrl`: 문제 설명용 이미지 (선택)

### INPUT (주관식)

```json
{
  "placeholder": "답을 입력하세요",
  "hintImageUrl": "https://s3.../mission/hint.png"
}
```

### PHOTO_CHECK (사진 체크)

```json
{
  "exampleImageUrl": "https://s3.../mission/example.png",
  "instruction": "이 장소를 찍어주세요"
}
```

---

## answer_json 권장 구조

### QUIZ

```json
{ "answer": "a" }
```
또는
```json
{ "value": "a" }
```

(정답 보기의 `id` 값)

### INPUT / PHOTO_CHECK

채점 로직에 따라 유연하게 사용. `value`, `expected` 등.

---

## media_assets 연동

이미지 URL은 `POST /api/v1/upload?type=image&category=mission` 으로 S3 업로드 후 사용합니다.
