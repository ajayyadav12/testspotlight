alter table T_RECEIVER add constraint receiver_unique unique("NAME");
alter table T_SENDER add constraint sender_unique unique("NAME");
alter table T_USER add constraint user_unique unique("SSO");
alter table T_PROCESS add constraint process_unique unique("NAME");
alter table T_NOTIFICATION_TEMPLATE add constraint t_notification_template_unique unique("NAME");
alter table T_DL_GROUP add constraint t_dl_group_unique unique("NAME");
alter table T_DL_GROUP_DTL add constraint t_dl_group_dtl_unique unique("DL_GROUP_ID", "EMAIL");

exit;