import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-liste-reunions',
  standalone: true,
  imports: [CommonModule],
  template: `<div class="page-container"><h1>Réunions</h1><p>Liste des réunions planifiées.</p></div>`,
  styles: [`.page-container { padding: 2rem; }`]
})
export class ListeReunionsComponent {}
