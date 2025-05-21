import socket
import threading
import sys

# Default configuration
DEFAULT_SERVER_IP = '127.0.0.1'
DEFAULT_SERVER_PORT = 6667
DUMMY_TARGET = "#channel"  # Dummy target for PRIVMSG

def receive_messages(sock):
    """
    Receives messages from the server and prints them.
    Runs in a separate thread.
    """
    while True:
        try:
            data = sock.recv(2048)
            if not data:
                print("Disconnected from server. (Received empty data)")
                sock.close()
                sys.exit() # Exit thread and program
            
            message = data.decode('utf-8').strip()
            print(message) # Print raw message from server

        except ConnectionResetError:
            print("Connection reset by server.")
            sock.close()
            sys.exit()
        except socket.error as e:
            print(f"Socket error during receive: {e}")
            sock.close()
            sys.exit()
        except Exception as e:
            print(f"Error receiving message: {e}")
            sock.close()
            sys.exit()

def send_messages(sock, nickname):
    """
    Handles user input and sends messages to the server.
    Runs in the main thread.
    """
    try:
        while True:
            user_input = input() # No prompt, just wait for input
            if user_input.strip().lower() == '/quit':
                quit_message = f"QUIT :Leaving\r\n"
                sock.send(quit_message.encode('utf-8'))
                sock.close()
                print("Disconnected by user command.")
                sys.exit() # Ensure program exits
            else:
                # Format as PRIVMSG
                # The server currently ignores the target, so DUMMY_TARGET is used
                message = f"PRIVMSG {DUMMY_TARGET} :{user_input}\r\n"
                sock.send(message.encode('utf-8'))
    except EOFError: # Handle Ctrl+D
        print("\nCtrl+D pressed. Sending QUIT and exiting...")
        quit_message = f"QUIT :Leaving (EOF)\r\n"
        try:
            sock.send(quit_message.encode('utf-8'))
        except socket.error:
            pass # Ignore if socket already closed
        finally:
            sock.close()
            sys.exit()
    except KeyboardInterrupt: # Handle Ctrl+C
        print("\nCtrl+C pressed. Sending QUIT and exiting...")
        quit_message = f"QUIT :Leaving (Interrupt)\r\n"
        try:
            sock.send(quit_message.encode('utf-8'))
        except socket.error:
            pass # Ignore if socket already closed
        finally:
            sock.close()
            sys.exit()
    except socket.error as e:
        print(f"Socket error during send: {e}")
        sock.close()
        sys.exit() # Ensure program exits if socket fails
    except Exception as e:
        print(f"Error sending message: {e}")
        sock.close()
        sys.exit()


if __name__ == "__main__":
    server_ip = input(f"Enter server IP (default {DEFAULT_SERVER_IP}): ") or DEFAULT_SERVER_IP
    try:
        server_port_str = input(f"Enter server port (default {DEFAULT_SERVER_PORT}): ") or str(DEFAULT_SERVER_PORT)
        server_port = int(server_port_str)
        if not (1024 <= server_port <= 65535):
            print("Invalid port number. Using default.")
            server_port = DEFAULT_SERVER_PORT
    except ValueError:
        print("Invalid port number. Using default.")
        server_port = DEFAULT_SERVER_PORT

    client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    try:
        print(f"Connecting to server at {server_ip}:{server_port}...")
        client_socket.connect((server_ip, server_port))
        print("Connected to server.")
    except socket.error as e:
        print(f"Could not connect to server: {e}")
        sys.exit(1)
    except Exception as e:
        print(f"An unexpected error occurred during connection: {e}")
        sys.exit(1)

    nickname = ""
    while not nickname.strip():
        nickname = input("Enter your nickname: ").strip()
        if not nickname:
            print("Nickname cannot be empty.")
        elif not nickname.matches("[a-zA-Z0-9_-]+"): # Basic validation similar to server
            print("Nickname contains invalid characters. Use letters, numbers, underscores, or hyphens.")
            nickname = ""


    try:
        # Send NICK command
        # Ensure it ends with \r\n as per IRC spec, though server might be lenient with \n
        client_socket.send(f"NICK {nickname}\r\n".encode('utf-8'))

        # Start the receiving thread
        receive_thread = threading.Thread(target=receive_messages, args=(client_socket,))
        receive_thread.daemon = True  # Dies when the main thread dies
        receive_thread.start()

        # Start the sending loop in the main thread
        send_messages(client_socket, nickname)

    except Exception as e: # Catch any other unexpected errors during setup/initial send
        print(f"An unexpected error occurred: {e}")
    finally:
        print("Closing client socket...")
        client_socket.close()
        sys.exit() # Ensure exit if send_messages returns for any reason not covered by its own sys.exit calls
