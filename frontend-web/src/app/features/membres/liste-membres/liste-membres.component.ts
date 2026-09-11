import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-liste-membres',
  standalone: true,
  imports: [CommonModule],
  template: `<div class="page-container"><h1>Membres</h1><p>Liste des membres du groupe.</p></div>`,
  styles: [`.page-container { padding: 2rem; }`]
})
export class ListeMembresComponent {}
