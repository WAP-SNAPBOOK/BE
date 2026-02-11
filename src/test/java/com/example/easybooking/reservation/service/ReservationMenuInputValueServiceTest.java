package com.example.easybooking.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.easybooking.reservation.ReservationMenuInputValueWriter;
import com.example.easybooking.reservation.domain.ReservationMenuInputValue;
import com.example.easybooking.reservation.dto.MenuInputValueRequest;
import com.example.easybooking.shop.ShopMenuInputFieldReader;
import com.example.easybooking.shop.domain.InputType;
import com.example.easybooking.shop.domain.ShopMenuInputField;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationMenuInputValueServiceTest {

    @Mock
    ShopMenuInputFieldReader inputFieldReader;

    @Mock
    ReservationMenuInputValueWriter inputValueWriter;

    @InjectMocks
    ReservationMenuInputValueService service;

    private ShopMenuInputField mockField(Long id, String label, InputType type, boolean required,
                                         BigDecimal min, BigDecimal max, BigDecimal step,
                                         Integer maxLength) {
        ShopMenuInputField field = mock(ShopMenuInputField.class);
        lenient().when(field.getId()).thenReturn(id);
        lenient().when(field.getLabel()).thenReturn(label);
        lenient().when(field.getInputType()).thenReturn(type);
        lenient().when(field.getRequired()).thenReturn(required);
        lenient().when(field.getMinValue()).thenReturn(min);
        lenient().when(field.getMaxValue()).thenReturn(max);
        lenient().when(field.getStepValue()).thenReturn(step);
        lenient().when(field.getMaxLength()).thenReturn(maxLength);
        return field;
    }

    // 4-D-1: 입력값 저장 + 스냅샷
    @Test
    void saveInputValues_persistsWithSnapshots() {
        ShopMenuInputField numberField = mockField(100L, "갯수", InputType.NUMBER, true,
                new BigDecimal("1"), new BigDecimal("10"), new BigDecimal("1"), null);
        when(inputFieldReader.findActiveByMenuId(10L)).thenReturn(List.of(numberField));
        when(inputValueWriter.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<ReservationMenuInputValue> result = service.saveInputValues(
                1L, 10L,
                List.of(new MenuInputValueRequest(100L, new BigDecimal("5"), null)));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReservationMenuItemId()).isEqualTo(1L);
        assertThat(result.get(0).getShopMenuInputFieldId()).isEqualTo(100L);
        assertThat(result.get(0).getFieldLabelSnapshot()).isEqualTo("갯수");
        assertThat(result.get(0).getInputTypeSnapshot()).isEqualTo("NUMBER");
        assertThat(result.get(0).getValueNumber()).isEqualByComparingTo(new BigDecimal("5"));
        assertThat(result.get(0).getValueText()).isNull();
    }

    // 4-D-2: required 필드 누락
    @Test
    void saveInputValues_throwsException_whenRequiredFieldMissing() {
        ShopMenuInputField requiredField = mockField(100L, "갯수", InputType.NUMBER, true,
                new BigDecimal("1"), new BigDecimal("10"), null, null);
        when(inputFieldReader.findActiveByMenuId(10L)).thenReturn(List.of(requiredField));

        assertThatThrownBy(() -> service.saveInputValues(1L, 10L, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("누락");
    }

    // 4-D-3: NUMBER 범위 검증
    @Nested
    class NumberRangeValidation {

        @Test
        void throwsException_whenValueBelowMin() {
            ShopMenuInputField field = mockField(100L, "갯수", InputType.NUMBER, true,
                    new BigDecimal("1"), new BigDecimal("10"), null, null);
            when(inputFieldReader.findActiveByMenuId(10L)).thenReturn(List.of(field));

            assertThatThrownBy(() -> service.saveInputValues(1L, 10L,
                    List.of(new MenuInputValueRequest(100L, new BigDecimal("0"), null))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("최솟값");
        }

        @Test
        void throwsException_whenValueAboveMax() {
            ShopMenuInputField field = mockField(100L, "갯수", InputType.NUMBER, true,
                    new BigDecimal("1"), new BigDecimal("10"), null, null);
            when(inputFieldReader.findActiveByMenuId(10L)).thenReturn(List.of(field));

            assertThatThrownBy(() -> service.saveInputValues(1L, 10L,
                    List.of(new MenuInputValueRequest(100L, new BigDecimal("15"), null))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("최댓값");
        }

        @Test
        void allowsBoundaryValues() {
            ShopMenuInputField field = mockField(100L, "갯수", InputType.NUMBER, true,
                    new BigDecimal("1"), new BigDecimal("10"), null, null);
            when(inputFieldReader.findActiveByMenuId(10L)).thenReturn(List.of(field));
            when(inputValueWriter.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            assertThat(service.saveInputValues(1L, 10L,
                    List.of(new MenuInputValueRequest(100L, new BigDecimal("1"), null)))).hasSize(1);
            assertThat(service.saveInputValues(1L, 10L,
                    List.of(new MenuInputValueRequest(100L, new BigDecimal("10"), null)))).hasSize(1);
        }
    }

    // 4-D-4: NUMBER step 검증
    @Nested
    class NumberStepValidation {

        @Test
        void throwsException_whenValueNotOnStep() {
            ShopMenuInputField field = mockField(100L, "갯수", InputType.NUMBER, true,
                    new BigDecimal("0"), new BigDecimal("100"), new BigDecimal("5"), null);
            when(inputFieldReader.findActiveByMenuId(10L)).thenReturn(List.of(field));

            assertThatThrownBy(() -> service.saveInputValues(1L, 10L,
                    List.of(new MenuInputValueRequest(100L, new BigDecimal("3"), null))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("step");
        }

        @Test
        void allowsValueOnStep() {
            ShopMenuInputField field = mockField(100L, "갯수", InputType.NUMBER, true,
                    new BigDecimal("0"), new BigDecimal("100"), new BigDecimal("5"), null);
            when(inputFieldReader.findActiveByMenuId(10L)).thenReturn(List.of(field));
            when(inputValueWriter.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            assertThat(service.saveInputValues(1L, 10L,
                    List.of(new MenuInputValueRequest(100L, new BigDecimal("10"), null)))).hasSize(1);
        }

        @Test
        void skipsStepValidation_whenStepValueNull() {
            ShopMenuInputField field = mockField(100L, "갯수", InputType.NUMBER, true,
                    new BigDecimal("0"), new BigDecimal("100"), null, null);
            when(inputFieldReader.findActiveByMenuId(10L)).thenReturn(List.of(field));
            when(inputValueWriter.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            assertThat(service.saveInputValues(1L, 10L,
                    List.of(new MenuInputValueRequest(100L, new BigDecimal("3"), null)))).hasSize(1);
        }
    }

    // 4-D-5: TEXT maxLength 검증
    @Nested
    class TextMaxLengthValidation {

        @Test
        void throwsException_whenTextExceedsMaxLength() {
            ShopMenuInputField field = mockField(200L, "요청사항", InputType.TEXT, true,
                    null, null, null, 5);
            when(inputFieldReader.findActiveByMenuId(10L)).thenReturn(List.of(field));

            assertThatThrownBy(() -> service.saveInputValues(1L, 10L,
                    List.of(new MenuInputValueRequest(200L, null, "여섯글자입니다"))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("최대 길이");
        }

        @Test
        void allowsBoundaryLength() {
            ShopMenuInputField field = mockField(200L, "요청사항", InputType.TEXT, true,
                    null, null, null, 5);
            when(inputFieldReader.findActiveByMenuId(10L)).thenReturn(List.of(field));
            when(inputValueWriter.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            assertThat(service.saveInputValues(1L, 10L,
                    List.of(new MenuInputValueRequest(200L, null, "다섯글자임")))).hasSize(1);
        }

        @Test
        void skipsLengthValidation_whenMaxLengthNull() {
            ShopMenuInputField field = mockField(200L, "요청사항", InputType.TEXT, true,
                    null, null, null, null);
            when(inputFieldReader.findActiveByMenuId(10L)).thenReturn(List.of(field));
            when(inputValueWriter.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            assertThat(service.saveInputValues(1L, 10L,
                    List.of(new MenuInputValueRequest(200L, null, "아무리 길어도 괜찮습니다 매우 긴 텍스트")))).hasSize(1);
        }
    }

    // 4-D-6: 타입 미스매치
    @Nested
    class TypeMismatchValidation {

        @Test
        void throwsException_whenNumberFieldHasOnlyText() {
            ShopMenuInputField field = mockField(100L, "갯수", InputType.NUMBER, true,
                    null, null, null, null);
            when(inputFieldReader.findActiveByMenuId(10L)).thenReturn(List.of(field));

            assertThatThrownBy(() -> service.saveInputValues(1L, 10L,
                    List.of(new MenuInputValueRequest(100L, null, "hello"))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("숫자 값");
        }

        @Test
        void throwsException_whenTextFieldHasOnlyNumber() {
            ShopMenuInputField field = mockField(200L, "요청사항", InputType.TEXT, true,
                    null, null, null, null);
            when(inputFieldReader.findActiveByMenuId(10L)).thenReturn(List.of(field));

            assertThatThrownBy(() -> service.saveInputValues(1L, 10L,
                    List.of(new MenuInputValueRequest(200L, new BigDecimal("5"), null))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("텍스트 값");
        }
    }

    // 4-D-7: 메뉴에 정의되지 않은 fieldId
    @Test
    void saveInputValues_throwsException_whenFieldNotBelongToMenu() {
        ShopMenuInputField field = mockField(100L, "갯수", InputType.NUMBER, false,
                null, null, null, null);
        when(inputFieldReader.findActiveByMenuId(10L)).thenReturn(List.of(field));

        assertThatThrownBy(() -> service.saveInputValues(1L, 10L,
                List.of(new MenuInputValueRequest(999L, new BigDecimal("5"), null))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("정의되지 않은");
    }
}
