import { MessageService } from 'primeng/api';
import { ProcessService } from './../../process.service';
import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-process-dtl-approve',
  template: 'Waiting for approval'
})
export class ProcessDtlApproveComponent implements OnInit {
  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private processSvc: ProcessService,
    private msgSvc: MessageService
  ) {
    const id = Number.parseInt(this.route.snapshot.paramMap.get('id'));
    if (id !== 0) {
      const approved = this.route.snapshot.queryParams.check;
      this.processSvc.approveProcess(id, approved).subscribe(value => {
        this.msgSvc.add({
          severity: 'success',
          summary: 'Process approved',
          detail: `Process ${value.name} is ready to rock!`
        });
        this.router.navigate(['/process']);
      });
    }
  }

  ngOnInit() {}
}
