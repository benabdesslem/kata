import {Component} from '@angular/core';
import {MessageModule} from "primeng/message";
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {CardModule} from "primeng/card";
import {NgClass, NgIf} from "@angular/common";
import {ButtonModule} from "primeng/button";
import {InputTextModule} from "primeng/inputtext";
import {InputTextareaModule} from "primeng/inputtextarea";
import {MessagesModule} from "primeng/messages";
import {Message, MessageService} from "primeng/api";

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [
    MessageModule,
    ReactiveFormsModule,
    CardModule,
    NgClass,
    ButtonModule,
    InputTextModule,
    InputTextareaModule,
    MessagesModule,
    NgIf
  ],
  templateUrl: './contact.component.html',
  styleUrl: './contact.component.css'
})
export class ContactComponent {
  contactForm!: FormGroup;
  emailError: Message[] = [
    {
      severity: 'error',
      detail: 'L\'email est requis et doit être valide.',
      closable: false,
    },
  ];
  messageError: Message[] = [
    {
      severity: 'error',
      detail: 'Le message est requis et doit contenir moins de 300 caractères.',
      closable: false,
    },
  ];
  isSubmitting = false;

  formStatusMessage: Message[] = [];

  constructor(
    private fb: FormBuilder,
  ) {
  }

  ngOnInit() {
    // Initialize the form with email and message controls
    this.contactForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]], // Email field with validation
      message: ['', [Validators.required, Validators.maxLength(300)]], // Message field with validation
    });


  }

  // Accessor methods to get form controls
  get email() {
    return this.contactForm.get('email');
  }

  get message() {
    return this.contactForm.get('message');
  }


  // Function called when the form is submitted
  onSubmit() {
    // Reset error messages before submission
    this.emailError = [];
    this.messageError = [];

    // Check if the form is invalid
    if (this.contactForm.invalid) {

      return;
    }

    this.isSubmitting = true;
    const {email, message} = this.contactForm.value;
    //TODO: Implement the logic to send the message

    // Simulate an HTTP request to send the message
    setTimeout(() => {
      this.isSubmitting = false;
      this.formStatusMessage = [{
        severity: 'success',
        detail: 'Your message has been sent successfully!',
      }];
      this.contactForm.reset();
    }, 2000);
  }
}
