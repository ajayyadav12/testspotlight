import { Component, OnInit, Renderer2, Inject } from '@angular/core';
import { DOCUMENT } from '@angular/common';

@Component({
  selector: 'app-systems-load',
  templateUrl: './systems-load.component.html',
  styleUrls: ['./systems-load.component.scss']
})
export class SystemsLoadComponent implements OnInit {

  constructor(private _renderer2: Renderer2, @Inject(DOCUMENT) private _document: Document) { }

  ngOnInit() {
    const script = this._renderer2.createElement('script');
    script.type = `application/javascript`;
    script.src = `https://oacdev1-gefinanceitpaas.analytics.ocp.oraclecloud.com/dv/ui/api/v1/plugins/embedding/standalone/embedding.js`;

    this._renderer2.appendChild(this._document.body, script);
  }

}
