import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-liste-cotisations',
  standalone: true,
  imports: [CommonModule],
  template: `<div class="page-container"><h1>Cotisations</h1><p>Suivi des cotisations.</p></div>`,
  styles: [`.page-container { padding: 2rem; }`]
})
export class ListeCotisationsComponent {}
