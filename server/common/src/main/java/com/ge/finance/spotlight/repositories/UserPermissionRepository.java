package com.ge.finance.spotlight.repositories;

import com.ge.finance.spotlight.models.Permission;
import com.ge.finance.spotlight.models.UserPermission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface UserPermissionRepository extends CrudRepository<UserPermission, Long>,
        JpaRepository<UserPermission, Long>, JpaSpecificationExecutor<UserPermission> {

    @Query("select up from UserPermission up join up.user u join up.permission p where u.sso = :sso and p.permission = :permission")
    Collection<UserPermission> findBySSOAndPermission(@Param("sso") Long sso, @Param("permission") String permission);

    @Query(value = " select up.sender_id, up.receiver_id, ss.name sender, rs.name receiver, up.read, up.submit, up2.id from "
            + " (select *  from spotlight_dba.t_user_permission "
            + " pivot ( count(id)  for permission  in ('1' as Read,'2' as Submit))) up , "
            + " spotlight_dba.t_user u, spotlight_dba.t_system ss, spotlight_dba.t_system rs, spotlight_dba.t_user_permission up2 "
            + " where u.id = up.user_id " + " and ss.id = up.sender_id " + " and rs.id = up.receiver_id "
            + " and up.sender_id = up2.sender_id " + " and up.receiver_id = up2.receiver_id "
            + " and up.user_id = up2.user_id " + " and up2.permission = 1 "
            + " and up.user_id = :userId ", nativeQuery = true)
    List<?> findByUserId(@Param("userId") Long userId);

    List<UserPermission> findByUserIdAndReceiverIdAndSenderId(Long userId, Long receiverId, Long senderId);

    UserPermission findByUserIdAndReceiverIdAndSenderIdAndPermission(Long userId, Long receiverId, Long senderId,
            Permission permission);
}
