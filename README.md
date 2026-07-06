# Space-Resource Booking 
A mySql, Java Swing application for convenient space and resource booking
Our application is designed to make booking meeting rooms, conference halls, and equipment seamless — just like ordering a pizza online, but way more productive! 

## Demo
https://drive.google.com/file/d/1z0lvKEV80YmjESTtp-gDf9dGviXsgGvC/view?usp=sharing

## Features
To make this possible, we designed a comprehensive database. Let me walk you through how we structured it:

* First, we have the Booking Sheet and Booked Items Log. These tables track every booking and every single item within it. So whether someone books a projector, a microphone, or an entire hall, it’s all accounted for.

* Then, our Users with Address table stores complete user details — not just names and emails, but addresses, making identification and notifications simple.

* Booking Ledger keeps track of transactions and total costs, separating payment details from booking details ensures clarity for both users and admins.

* We also have Space and Resource tables, our master inventories. Without these, our application would be like a store with invisible shelves — nothing would be available to book!

* Reviews let users provide feedback, helping future users make informed decisions.

* Maintenance Schedule prevents double-bookings or scheduling conflicts for unavailable resources, because nobody wants a projector booked while it’s being repaired.

* Finally, Roles define user permissions — ensuring admins, employees, and students have the right access, keeping the system secure and manageable.
  
## Tech Stack
- RDBMS-MYSQL
- GUI- Java Swing

## Contributing
Contributions are welcome! Please open an issue or send a pull request.
## Team
- Shreya Babar - @s-hreya184
- Srushti Kotgire
- Vidhisha Kulkarni
- Gauri Patil

