# Notifications Service
A comprehensive, multi-channel notification service built with Spring Boot that supports various communication channels including email, SMS, push notifications, WhatsApp, and in-app notifications.

## Project Structure
```
src/main/java/com/e_commerce/notifica/
├── config/                    
│   ├── AsyncConfig.java          
│   ├── NotificationConfig.java   
│   ├── RetryConfig.java          
│   └── WebSocketConfig.java      
├── controllers/          
│   ├── NotificationController.java          
│   └── WebSocketNotificationController.java 
|── dtos/
|   └── request/
|       └── NotificationRequest.java
|   └── response/
|       └── NotificationResponse.java|
|── exceptions/             
│   ├── ChannelNotSupportedException.java     
│   ├── NotificationException.java       
│   ├── NotificationNotFoundException.java       
│   ├── NotificationSendException.java       
│   ├── ProviderConfigurationException.java       
│   ├── RetryExhaustedException.java       
│   ├── ValidationException.java       
│   ├── ErrorResponse.java       
│   └── GlobalExceptionHandler.java    
├── models/              
│   ├── enums/
│   │   ├── ChannelType.java              
│   │   ├── NotificationPriority.java     
│   │   ├── NotificationStatus.java       
│   │   └── NotificationType.java         
│   ├── InAppNotification.java            
│   └── NotificationLog.java              
├── repositories/   
|   ├── InAppNotificationRepository.java          
|   └── NotificationLogRepository.java           
├── services/                 
│   ├── EmailService.java                 
│   ├── InAppNotificationService.java     
│   ├── NotificationService.java          
│   ├── NotificationStatsService.java     
│   ├── PushNotificationService.java      
│   ├── SmsService.java                   
│   ├── WebSocketService.java             
│   └── WhatsAppService.java              
├── strategies/               
│   ├── EmailNotificationStrategy.java    
│   ├── InAppNotificationStrategy.java    
│   ├── NotificationStrategy.java         
│   ├── PushNotificationStrategy.java     
│   └── SmsNotificationStrategy.java      
├── validation/               
│   ├── RecipientChannelValidator.java    
│   └── ValidRecipientForChannel.java     
└── NotificationsServiceApplication.java  
```

## Features
### Supported Channels
- **Email**: SMTP-based email notifications
- **SMS**: Text message notifications
- **Push Notifications**: Mobile and web push notifications
- **WhatsApp**: WhatsApp message integration
- **In-App**: Real-time in-app notifications with WebSocket support

### Core Functionality
- **Multi-channel Strategy**: Strategy pattern for flexible notification delivery
- **Async Processing**: Asynchronous notification processing for better performance
- **Retry Mechanism**: Configurable retry logic for failed notifications
- **Real-time Updates**: WebSocket support for live notification updates
- **Validation**: Comprehensive recipient and channel validation
- **Status Tracking**: Complete notification lifecycle tracking
- **Priority System**: Support for different notification priorities

## Configuration
### Key Configuration Files
- **AsyncConfig**: Configures asynchronous processing
- **RetryConfig**: Sets up retry mechanisms for failed notifications
- **WebSocketConfig**: Configures WebSocket endpoints for real-time communication
- **NotificationConfig**: Main notification service configuration

## Usage
### Sending Notifications
REST API
java
```
// Example using NotificationController
POST /api/notifications
{
    "type": "ORDER_CONFIRMATION",
    "priority": "HIGH",
    "channels": ["EMAIL", "SMS"],
    "recipient": "user@example.com",
    "subject": "Order Confirmed",
    "message": "Your order has been confirmed",
    "metadata": {
        "orderId": "12345"
    }
}
```

### Programmatic Usage
java
```
@Autowired
private NotificationService notificationService;

// Send notification
notificationService.sendNotification(notificationRequest);
WebSocket Integration
Connect to WebSocket endpoint for real-time notifications:

javascript
const socket = new WebSocket('ws://localhost:8080/notifications');
socket.onmessage = function(event) {
    const notification = JSON.parse(event.data);
    // Handle real-time notification
};
```

## Models
### Core Entities
- **InAppNotification**: Stores in-app notification data
- **NotificationLog**: Tracks all notification attempts and status
- **NotificationStatus**: Tracks delivery status (PENDING, SENT, FAILED, etc.)
- **NotificationPriority**: Priority levels (LOW, MEDIUM, HIGH, URGENT)

### Enums
- **ChannelType**: Supported notification channels
- **NotificationType**: Types of notifications (SYSTEM, MARKETING, etc.)

## Services
- **NotificationService**: Main service coordinating notification delivery
- **EmailService**: Handles email notifications
- **PushNotificationService**: Handles push notifications
- **WhatsAppService**: Manages WhatsApp messages
- **InAppNotificationService**: Manages in-app notifications
- **WebSocketService**: Handles real-time WebSocket communication