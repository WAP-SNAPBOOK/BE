import type { StoreConfig } from "@/types/booking"
import { FieldType } from "@/types/booking"

export const FALLBACK_CONFIGS: Record<string, StoreConfig> = {
  "1": {
    id: "1",
    name: "",
    title: "예약 신청서",
    formFields: [
      { fieldId: "name", label: "성함", type: FieldType.TEXT, required: true, placeholder: "성함을 입력해주세요" },
      { fieldId: "contact", label: "연락처", type: FieldType.TEXT, required: true, placeholder: "010-0000-0000" },
      { fieldId: "date", label: "예약 날짜", type: FieldType.TEXT, required: true },
      { fieldId: "time", label: "예약 시간", type: FieldType.TEXT, required: true },
      {
        fieldId: "removal",
        label: "제거",
        type: FieldType.RADIO,
        required: false,
        options: ["필요없음", "자샵", "타샵"],
      },
      {
        fieldId: "treatment",
        label: "원하는 시술 항목",
        type: FieldType.RADIO,
        required: true,
        options: ["네일", "패디"],
      },
      { fieldId: "wrapping", label: "래핑 개수", type: FieldType.NUMBER, required: false, min: 0, max: 10 },
      { fieldId: "extension", label: "연장 개수", type: FieldType.NUMBER, required: false, min: 0, max: 10 },
      { fieldId: "photos", label: "디자인 사진 첨부", type: FieldType.FILE, required: false, accept: "image/*" },
    ],
  },
  "2": {
    id: "2",
    name: "",
    title: "예약 신청서",
    formFields: [
      { fieldId: "name", label: "성함", type: FieldType.TEXT, required: true, placeholder: "성함을 입력해주세요" },
      { fieldId: "contact", label: "연락처", type: FieldType.TEXT, required: true, placeholder: "010-0000-0000" },
      { fieldId: "date", label: "예약 날짜", type: FieldType.TEXT, required: true },
      { fieldId: "time", label: "예약 시간", type: FieldType.TEXT, required: true },
      {
        fieldId: "treatment",
        label: "원하는 시술",
        type: FieldType.RADIO,
        required: true,
        options: ["속눈썹 연장", "속눈썹 펌", "눈썹 정리"],
      },
      { fieldId: "photos", label: "참고 사진 첨부", type: FieldType.FILE, required: false, accept: "image/*" },
    ],
  },
}
