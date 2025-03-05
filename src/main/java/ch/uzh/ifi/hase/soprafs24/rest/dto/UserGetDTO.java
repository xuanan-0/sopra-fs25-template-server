package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import java.util.Date;


public class UserGetDTO {

  private Long id;
  private String username;
  private Date creationDate;
  private Date birthday;
  private UserStatus status;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Date getCreationDate() { 
    return creationDate; 
  } 
  
  public void setCreationDate(Date creationDate) { 
    this.creationDate = creationDate; 
  } 

  public Date getBirthday() {
     return birthday; 
  } 
  
  public void setBirthday(Date birthday) {
     this.birthday = birthday; 
  } 

  public UserStatus getStatus() {
    return status;
  }

  public void setStatus(UserStatus status) {
    this.status = status;
  }
}
