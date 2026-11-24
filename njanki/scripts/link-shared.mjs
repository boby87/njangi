import { existsSync, lstatSync, symlinkSync } from 'node:fs';
import { resolve } from 'node:path';

const target = resolve(import.meta.dirname, '../../shared');
const linkPath = resolve(import.meta.dirname, '../.shared');

if (!existsSync(linkPath)) {
  try {
    lstatSync(linkPath);
  } catch {
    symlinkSync(target, linkPath, process.platform === 'win32' ? 'junction' : 'dir');
  }
}
