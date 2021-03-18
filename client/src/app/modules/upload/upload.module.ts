import { NgModule } from '@angular/core';
import { UploadComponent } from './upload.component';
import { UploadRoutingModule } from './upload-routing.module';
import { CommonModule } from '@angular/common';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
    declarations: [UploadComponent],
    imports: [CommonModule, UploadRoutingModule, PrimengModule, ReactiveFormsModule]
})
export class UploadModule {}