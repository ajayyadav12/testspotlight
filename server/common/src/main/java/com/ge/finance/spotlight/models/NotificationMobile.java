package com.ge.finance.spotlight.models;

import javax.persistence.*;

@Entity
@Table(name = "t_notification_def_mobile")
public class NotificationMobile {
	 
	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_NOTIFICATION_MOBILE_ID")
    @SequenceGenerator(name = "S_NOTIFICATION_MOBILE_ID", sequenceName = "S_NOTIFICATION_MOBILE_ID", allocationSize = 1)
    private Long id;
	@Column(name = "notification_def_id")
	private Long notificationId;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

	public Long getNotificationId() {
		return notificationId;
	}
	public void setNotificationId(Long notificationId) {
		this.notificationId = notificationId;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
        
}
