package com.example.easybooking.form;

import com.example.easybooking.errors.errorcode.AuthErrorCode;
import com.example.easybooking.errors.exception.AuthException;
import com.example.easybooking.form.domain.Form;
import com.example.easybooking.form.domain.FormCopyUtil;
import com.example.easybooking.form.dto.FormResponse;
import com.example.easybooking.form.dto.request.FormPatchRequest;
import com.example.easybooking.form.mapper.FormMapper;
import com.example.easybooking.shop.ShopReader;
import com.example.easybooking.shop.domain.Shop;
import com.example.easybooking.user.UserReader;
import com.example.easybooking.user.domain.User;
import com.example.easybooking.user.domain.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.access.AccessDeniedException;

@Service
@RequiredArgsConstructor
public class FormService {

    private final FormReader formReader;
    private final FormMapper formMapper;
    private final FormCopyUtil formCopyUtil;
    private final FormPatcher formPatcher;
    private final ShopReader shopReader;
    private final UserReader userReader;

    public FormResponse getForm(Long shopId, Long userId){
        // 사용자 정보 및 UserType 조회
        User user = userReader.read(userId);
        UserType userType = user.getUserType();

        // 점주(OWNER) 요청시 소유권 검증
        if (userType == UserType.OWNER) {
            Shop shop = shopReader.read(shopId);

            if (!shop.getOwnerId().equals(userId)) {
                throw new AuthException(AuthErrorCode.UNAUTHENTICATED_USER,"점주는 본인이 소유한 매장의 폼만 조회할 수 있습니다.");
            }
        }

        Form form = formReader.readByShopId(shopId);
        return formMapper.toDto(form);
    }

    @Transactional
    public void createDefaultForm(Long shopId) {
        formCopyUtil.copyDefaultFormToShop(shopId);
    }

    public void patchForm(Long shopId, Long ownerUserId, FormPatchRequest request) {
        // 소유권 검증
        // 이 샵이 현재 로그인된 점주의 소유인지 확인
        Shop shop = shopReader.read(shopId);
        if (!shop.getOwnerId().equals(ownerUserId)) {
            throw new AuthException(AuthErrorCode.UNAUTHENTICATED_USER, "폼을 수정할 권한이 없습니다. (점주 불일치)");
        }

        Form form = formReader.readByShopId(shopId);
        formPatcher.patch(form, request);
    }
}
