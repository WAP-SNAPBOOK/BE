package com.example.easybooking.shop.service;

import com.example.easybooking.shop.domain.ShopMenuTag;
import com.example.easybooking.shop.domain.Tag;
import com.example.easybooking.shop.dto.response.TagResponse;
import com.example.easybooking.shop.repository.ShopMenuTagRepository;
import com.example.easybooking.shop.repository.TagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;
    private final ShopMenuTagRepository shopMenuTagRepository;

    @Transactional
    public TagResponse createOrGet(String name) {
        Tag tag = tagRepository.findByName(name)
                .orElseGet(() -> tagRepository.save(Tag.create(name)));
        return new TagResponse(tag);
    }

    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(TagResponse::new)
                .toList();
    }

    @Transactional
    public void addTagToMenu(Long menuId, Long tagId) {
        shopMenuTagRepository.save(ShopMenuTag.create(menuId, tagId));
    }

    @Transactional
    public void removeTagFromMenu(Long menuId, Long tagId) {
        shopMenuTagRepository.deleteByShopMenuIdAndTagId(menuId, tagId);
    }
}
