package com.ge.finance.spotlight.libs;

import com.ge.finance.spotlight.models.User;
import com.ge.finance.spotlight.models.Process;;

public class AcknowledgeLib{

    public static boolean IsAllowedToAcknowledge(Process process, Long sso){
        User processAppUser = process.getAppOwner();
        User senderAppUser= null; 
        if(process.getSender() != null){
            senderAppUser = process.getSender().getAppOwner();
        }
        if ((processAppUser != null && processAppUser.getSso().equals(sso)) || (senderAppUser != null && senderAppUser.getSso().equals(sso)) ) {            
            return true;
        } else {
            return false;
        }   
    }    
}