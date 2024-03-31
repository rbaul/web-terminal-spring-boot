import {AfterViewInit, Component, ElementRef, OnChanges, OnInit, QueryList, SimpleChanges, ViewChild, ViewChildren, ViewContainerRef, ViewEncapsulation} from '@angular/core';
import {FormControl} from '@angular/forms';
import {Terminal} from 'xterm';
import { FitAddon } from 'xterm-addon-fit';
import { WebLinksAddon } from 'xterm-addon-web-links';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  encapsulation: ViewEncapsulation.None,
})
export class AppComponent implements OnInit, AfterViewInit, OnChanges {

  @ViewChild('terminal', {static: false}) terminal!: ElementRef;
  @ViewChildren('terminal', { read: ViewContainerRef }) frame!: QueryList<ViewContainerRef>;

  title = 'web-terminal-frontend';
  tabs: string[] = [];
  terminals: Terminal[] = [];
  selected = new FormControl(0);

  constructor() {

    // const attachAddon = new AttachAddon(webSocket);
    // terminal.loadAddon(attachAddon);
  }
  ngOnChanges(changes: SimpleChanges): void {
    console.log('ngOnChanges');
  }

  ngOnInit(): void {
    // throw new Error('Method not implemented.');
  }

  ngAfterViewInit(): void {
    console.log('ngAfterViewInit');
  }

  addTab() {
    this.tabs.push('New');

    const term = new Terminal({
      cols: 200,
      rows: 37,
      cursorBlink: true,
      cursorStyle: "block",
      scrollback: 50000,
      tabStopWidth: 8
    });
    const fitAddon = new FitAddon();
    term.loadAddon(new WebLinksAddon());
    term.loadAddon(fitAddon);

    this.terminals.push(term);


    let elementById = document.getElementById(`terminal-0`);
    this.frame.get(0)?.createComponent
    if (elementById) {
      term.open(elementById);
      term.options.theme
      fitAddon.fit();
      term.write('Hello from \x1B[1;3;31mxterm.js\x1B[0m \n\r$ ')
      term.onKey( (key, ev) => {
        console.log(key.key);
        if (key.key === '\r') {
          term.write('\n\r$ ');
        } else if (key.key === '\x1B[A') { // UP

        } else if (key.key === '\x1B[B') { // DOWN

        } else if (key.key === '\x1B[D') { // LEFT

        } else if (key.key === '\x1B[C') { // RIGHT

        } else {
          term.write(key.key);
        }

      });
    }

    this.selected.setValue(this.tabs.length - 1);

  }

  tabCreated() {
    console.log('tab created');
  }

  removeTab(index: number) {
    this.tabs.splice(index, 1);
    this.terminals.splice(index, 1);
  }
}
