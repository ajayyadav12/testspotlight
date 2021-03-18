import { Directive, ElementRef, Input, AfterViewInit } from '@angular/core';

@Directive({
	selector: '[autofocus]'
})
export class AutofocusDirective implements AfterViewInit {
	private _autofocus;
	constructor(private elementRef: ElementRef) {}

	ngAfterViewInit(): void {
		if (this._autofocus || typeof this._autofocus === 'undefined') {
			this.elementRef.nativeElement.focus();
		}
	}

	@Input()
	set autofocus(condition: boolean) {
		this._autofocus = condition !== false;
	}
}
