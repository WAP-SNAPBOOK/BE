package com.example.easybooking.form;

public class FormParsingUtil {
    /**
     * 문자열을 Integer로 안전하게 파싱합니다.
     *
     * @param value 파싱할 문자열
     * @return 파싱된 Integer 값. null이거나 빈 문자열인 경우 null 반환.
     * @throws NumberFormatException 값이 null이 아니고 빈 문자열이 아니지만 유효한 정수가 아닌 경우
     */
    public static Integer parseSafeInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;  // 빈 값은 null 반환 (선택 필드이므로 허용)
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            // 잘못된 형식은 예외를 다시 던져서 명시적으로 처리
            throw new NumberFormatException(
                    String.format("유효하지 않은 숫자 형식입니다: '%s'", value)
            );
        }
    }
}
