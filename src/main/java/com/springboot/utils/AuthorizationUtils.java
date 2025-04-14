package com.springboot.utils;

import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

;

public class AuthorizationUtils {

    // 관리자인지 확인하는 메서드
    // 현재 인증된 사용자의 권한 목록에서 "ROLE_ADMIN" 권한이 있는지 확인한다.
    public static boolean isAdmin() {
        //SecurityContextHolder를 통해 현재 사용자의 인증 정보를 가져온다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //인증 객체에서 사용자의 권한 목록을 스트림으로 반환
        //권한 중 "ROLE_ADMIN과 일치하는지 검사
        return authentication.getAuthorities().stream()
                //ROLE_ADMIN 확인
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }

    public static void verifyAdmin(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))){
            throw new BusinessLogicException(ExceptionCode.UNAUTHORIZED_OPERATION);
        }
    }

    // 관리자인지 또는 동일한 사용자인지 확인하고 아니면 예외 던지는 메서드
    public static void isAdminOrOwner(long ownerId, long authenticatedId) {
        if (!isOwner(ownerId, authenticatedId) || !isAdmin()) {
            throw new BusinessLogicException(ExceptionCode.UNAUTHORIZED_OPERATION);
        }
    }

    // 로그인한 사용자와 작성자가 동일한지 확인하는 메서드
    public static boolean isOwner(long ownerId, long authenticatedId) {
        if (ownerId != authenticatedId) {
            throw new BusinessLogicException(ExceptionCode.UNAUTHORIZED_OPERATION);
        }
        return true;
    }

    //작성자가 관리자인지 확인하고 아니라면 예외 던지는 메서드
    public static void verifyAuthorIsAdmin(long memberId, long adminId){

        //현재 인증된 사용자와 권한 비교
        if(!isAdmin() && memberId != adminId) {
            throw new BusinessLogicException(ExceptionCode.UNAUTHORIZED_OPERATION);
        }
    }
}
