import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { CoreModule } from './core/core.module';
import { SidebarComponent } from './sidebar/sidebar.component';
import { AdminModule } from './modules/admin/admin.module';
import { ReportsModule } from './modules/reports/reports.module';
import { ToastModule } from 'primeng/toast';
import { LoginModule } from './modules/login/login.module';
import { MessagesModule } from 'primeng/messages';
import { MessageModule } from 'primeng/message';
import { MenuModule } from 'primeng/menu';
import { TooltipModule } from 'primeng/tooltip';
import { TopbarComponent } from './topbar/topbar.component';
import { UploadModule } from './modules/upload/upload.module';
import { OverlayPanelModule } from 'primeng/overlaypanel';
import { ListboxModule } from 'primeng/listbox';
import { GoogleAnalyticsService } from './google-analytics.service';

@NgModule({
	declarations: [AppComponent, SidebarComponent, TopbarComponent],
	imports: [
		BrowserModule,
		BrowserAnimationsModule,
		AppRoutingModule,
		AdminModule,
		ReportsModule,
		HttpClientModule,
		CoreModule,
		ToastModule,
		LoginModule,
		MessagesModule,
		MessageModule,
		TooltipModule,
		MenuModule,
		UploadModule,
		OverlayPanelModule,
		ListboxModule
	],
	providers: [GoogleAnalyticsService],
	bootstrap: [AppComponent]
})
export class AppModule { }
