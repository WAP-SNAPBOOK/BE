import { FormBuilder } from "@/components/form-builder"

export default function Home() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      <div className="container mx-auto px-4 py-8">
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-gray-900 mb-4">스마트 예약 양식 생성기</h1>
          <p className="text-lg text-gray-600 max-w-2xl mx-auto">
            텍스트를 입력하면 자동으로 예약 양식을 생성해드립니다. 네일샵, 미용실, 병원 등 다양한 예약 양식을 쉽게
            만들어보세요.
          </p>
        </div>
        <FormBuilder />
      </div>
    </div>
  )
}
