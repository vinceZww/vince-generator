package com.vince.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vince.web.common.ErrorCode;
import com.vince.web.constant.CommonConstant;
import com.vince.web.exception.BusinessException;
import com.vince.web.exception.ThrowUtils;
//import com.vince.web.mapper.GeneratorFavourMapper;
import com.vince.web.mapper.GeneratorMapper;
//import com.vince.web.mapper.GeneratorThumbMapper;
import com.vince.web.model.dto.generator.GeneratorQueryRequest;
import com.vince.web.model.entity.Generator;
//import com.vince.web.model.entity.GeneratorFavour;
//import com.vince.web.model.entity.GeneratorThumb;
import com.vince.web.model.entity.User;
import com.vince.web.model.vo.GeneratorVO;
import com.vince.web.model.vo.UserVO;
import com.vince.web.service.GeneratorService;
import com.vince.web.service.UserService;
import com.vince.web.utils.SqlUtils;
import io.netty.util.internal.ThrowableUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 帖子服务实现
 *
 *
 *
 */
@Service
@Slf4j
public class GeneratorServiceImpl extends ServiceImpl<GeneratorMapper, Generator> implements GeneratorService {

    @Resource
    private UserService userService;


    @Override
    public void validGenerator(Generator generator, boolean add) {
        if (generator == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String name = generator.getName();
        String description = generator.getDescription();
        String tags = generator.getTags();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(name, description), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(name) && name.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "名称过长");
        }
        if (StringUtils.isNotBlank(description) && description.length() > 256) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "描述过长");
        }
    }

    /**
     * 获取查询包装类
     *
     * @param generatorQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Generator> getQueryWrapper(GeneratorQueryRequest generatorQueryRequest) {
        QueryWrapper<Generator> queryWrapper = new QueryWrapper<>();
        if (generatorQueryRequest == null) {
            return queryWrapper;
        }
        String searchText = generatorQueryRequest.getSearchText();
        String sortField = generatorQueryRequest.getSortField();
        String sortOrder = generatorQueryRequest.getSortOrder();
        Long id = generatorQueryRequest.getId();

        String name = generatorQueryRequest.getName();
        String description = generatorQueryRequest.getDescription();
        List<String> tagList = generatorQueryRequest.getTags();
        Integer status = generatorQueryRequest.getStatus();

        Long userId = generatorQueryRequest.getUserId();
        Long notId = generatorQueryRequest.getNotId();
        // 拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.like("name", searchText).or().like("description", searchText);
        }
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }



    @Override
    public GeneratorVO getGeneratorVO(Generator generator, HttpServletRequest request) {
        GeneratorVO generatorVO = GeneratorVO.objToVo(generator);
        /*long generatorId = generator.getId();
        // 1. 关联查询用户信息
        Long userId = generator.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        generatorVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<GeneratorThumb> generatorThumbQueryWrapper = new QueryWrapper<>();
            generatorThumbQueryWrapper.in("generatorId", generatorId);
            generatorThumbQueryWrapper.eq("userId", loginUser.getId());
            GeneratorThumb generatorThumb = generatorThumbMapper.selectOne(generatorThumbQueryWrapper);
            generatorVO.setHasThumb(generatorThumb != null);
            // 获取收藏
            QueryWrapper<GeneratorFavour> generatorFavourQueryWrapper = new QueryWrapper<>();
            generatorFavourQueryWrapper.in("generatorId", generatorId);
            generatorFavourQueryWrapper.eq("userId", loginUser.getId());
            GeneratorFavour generatorFavour = generatorFavourMapper.selectOne(generatorFavourQueryWrapper);
            generatorVO.setHasFavour(generatorFavour != null);
        }*/
        return generatorVO;
    }

    @Override
    public Page<GeneratorVO> getGeneratorVOPage(Page<Generator> generatorPage, HttpServletRequest request) {
        List<Generator> generatorList = generatorPage.getRecords();
        Page<GeneratorVO> generatorVOPage = new Page<>(generatorPage.getCurrent(), generatorPage.getSize(), generatorPage.getTotal());
        /*if (CollUtil.isEmpty(generatorList)) {
            return generatorVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = generatorList.stream().map(Generator::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> generatorIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> generatorIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> generatorIdSet = generatorList.stream().map(Generator::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<GeneratorThumb> generatorThumbQueryWrapper = new QueryWrapper<>();
            generatorThumbQueryWrapper.in("generatorId", generatorIdSet);
            generatorThumbQueryWrapper.eq("userId", loginUser.getId());
            List<GeneratorThumb> generatorGeneratorThumbList = generatorThumbMapper.selectList(generatorThumbQueryWrapper);
            generatorGeneratorThumbList.forEach(generatorGeneratorThumb -> generatorIdHasThumbMap.put(generatorGeneratorThumb.getGeneratorId(), true));
            // 获取收藏
            QueryWrapper<GeneratorFavour> generatorFavourQueryWrapper = new QueryWrapper<>();
            generatorFavourQueryWrapper.in("generatorId", generatorIdSet);
            generatorFavourQueryWrapper.eq("userId", loginUser.getId());
            List<GeneratorFavour> generatorFavourList = generatorFavourMapper.selectList(generatorFavourQueryWrapper);
            generatorFavourList.forEach(generatorFavour -> generatorIdHasFavourMap.put(generatorFavour.getGeneratorId(), true));
        }
        // 填充信息
        List<GeneratorVO> generatorVOList = generatorList.stream().map(generator -> {
            GeneratorVO generatorVO = GeneratorVO.objToVo(generator);
            Long userId = generator.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            generatorVO.setUser(userService.getUserVO(user));
            generatorVO.setHasThumb(generatorIdHasThumbMap.getOrDefault(generator.getId(), false));
            generatorVO.setHasFavour(generatorIdHasFavourMap.getOrDefault(generator.getId(), false));
            return generatorVO;
        }).collect(Collectors.toList());
        generatorVOPage.setRecords(generatorVOList);*/
        return generatorVOPage;
    }

}




