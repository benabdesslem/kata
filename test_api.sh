#!/bin/bash

# Function to create a user
create_user() {
    echo "Creating user..."
    response=$(curl -s -X 'POST' \
    'http://localhost:8080/api/account' \
    -H 'accept: */*' \
    -H "Authorization: Bearer $jwt_token" \
    -H 'Content-Type: application/json' \
    -d '{
        "username": "marwen",
        "firstname": "marwen",
        "email": "admin@admin.com",
        "password": "password456"
    }')
    echo "User creation response: $response"
}

# Function to get JWT token
get_jwt_token() {
    echo "Getting JWT token..."
    response=$(curl -s -X 'POST' \
    'http://localhost:8080/api/token' \
    -H 'accept: */*' \
    -H 'Content-Type: application/json' \
    -d '{
      "email": "admin@admin.com",
      "password": "password456"
    }')

    echo "JWT token response: $response"
    # Use grep and sed to extract the token from the response
    jwt_token=$(echo $response | grep -oP '"token":"\K[^"]+')
    
    if [[ -z "$jwt_token" || "$jwt_token" == "null" ]]; then
        echo "Error: Could not retrieve JWT token."
        exit 1
    fi
    echo "JWT Token: $jwt_token"
}

# Function to create a product
create_product() {
    echo "Creating product..."
    response=$(curl -s -X 'POST' \
    'http://localhost:8080/api/products' \
    -H "accept: */*" \
    -H "Authorization: Bearer $jwt_token" \
    -H 'Content-Type: application/json' \
    -d '{
      "id": null,
      "code": "PRD001",
      "name": "Laptop",
      "description": "High-performance laptop with 16GB RAM and 512GB SSD.",
      "image": "https://example.com/images/laptop.png",
      "category": "Electronics",
      "price": 1299.99,
      "quantity": 50,
      "internalReference": "LPT-2024",
      "shellId": 10,
      "inventoryStatus": "INSTOCK",
      "rating": 4.8,
      "createdAt": "2024-12-28T12:00:00.000Z",
      "updatedAt": "2024-12-29T10:15:00.000Z"
    }')
    echo "Product creation response: $response"
}

# Function to get products
get_products() {
    echo "Getting products..."
    response=$(curl -s -X 'GET' \
    'http://localhost:8080/api/products' \
    -H "accept: */*" \
    -H "Authorization: Bearer $jwt_token")
    echo "Product list response: $response"
}

# Function to update a product
update_product() {
    product_id=$1
    echo "Updating product with ID $product_id..."
    response=$(curl -s -X 'PATCH' \
    "http://localhost:8080/api/products/$product_id" \
    -H "accept: */*" \
    -H "Authorization: Bearer $jwt_token" \
    -H 'Content-Type: application/json' \
    -d '{
      "id": 1,
      "code": "PRD001",
      "name": "Laptop",
      "description": "Updated description: Sleek and powerful laptop with enhanced graphics performance.",
      "image": "https://example.com/images/laptop.png",
      "category": "Electronics",
      "price": 1299.99,
      "quantity": 50,
      "internalReference": "LPT-2024",
      "shellId": 10,
      "inventoryStatus": "INSTOCK",
      "rating": 4.8,
      "createdAt": "2024-12-29T14:37:46.583427+01:00",
      "updatedAt": "2024-12-29T10:15:00Z"
    }')
    echo "Product update response: $response"
}

# Function to delete a product
delete_product() {
    product_id=$1
    echo "Deleting product with ID $product_id..."
    response=$(curl -s -X 'DELETE' \
    "http://localhost:8080/api/products/$product_id" \
    -H "accept: */*" \
    -H "Authorization: Bearer $jwt_token")
    echo "Product deletion response: $response"
}

# Function to add product to cart
add_to_cart() {
    product_id=$1
    echo "Adding product ID $product_id to cart..."
    response=$(curl -s -X 'POST' \
    "http://localhost:8080/api/cart/add?productId=$product_id" \
    -H "accept: */*" \
    -H "Authorization: Bearer $jwt_token" \
    -d '')
    echo "Add to cart response: $response"
}

# Function to reduce product quantity in cart
reduce_cart_quantity() {
    product_id=$1
    echo "Reducing quantity of product ID $product_id in cart..."
    response=$(curl -s -X 'PATCH' \
    "http://localhost:8080/api/cart/reduce-product-quantity?productId=$product_id" \
    -H "accept: */*" \
    -H "Authorization: Bearer $jwt_token")
    echo "Reduce cart quantity response: $response"
}

# Function to remove product from cart
remove_from_cart() {
    product_id=$1
    echo "Removing product ID $product_id from cart..."
    response=$(curl -s -X 'DELETE' \
    "http://localhost:8080/api/cart/remove/$product_id" \
    -H "accept: */*" \
    -H "Authorization: Bearer $jwt_token")
    echo "Remove from cart response: $response"
}

# Function to handle wishlist operations
add_to_wishlist() {
    product_id=$1
    echo "Adding product ID $product_id to wishlist..."
    response=$(curl -s -X 'POST' \
    'http://localhost:8080/api/wishlist/add' \
    -H 'accept: */*' \
    -H "Authorization: Bearer $jwt_token" \
    -H 'Content-Type: application/json' \
    -d "{
      \"productId\": $product_id
    }")
    echo "Add to wishlist response: $response"
}

remove_from_wishlist() {
    product_id=$1
    echo "Removing product ID $product_id from wishlist..."
    response=$(curl -s -X 'DELETE' \
    "http://localhost:8080/api/wishlist/remove?productId=$product_id" \
    -H 'accept: */*' \
    -H "Authorization: Bearer $jwt_token")
}

check_cart() {
  echo "Checking cart..."
  curl -s -X 'GET' \
    'http://localhost:8080/api/cart' \
    -H "accept: */*" \
    -H "Authorization: Bearer $jwt_token"
}

check_wishlist() {
  echo "Checking wishlist..."
  curl -s -X 'GET' \
    'http://localhost:8080/api/wishlist' \
    -H 'accept: */*' \
    -H "Authorization: Bearer $jwt_token"
}

get_cart_id() {
  echo "Fetching cart ID..."
  response=$(curl -s -X 'GET' \
    'http://localhost:8080/api/cart' \
    -H "accept: */*" \
    -H "Authorization: Bearer $jwt_token")

  echo "Cart API response: $response"

  # Utiliser grep et sed pour extraire l'ID du panier depuis la réponse JSON
  cart_id=$(echo "$response" | grep -oP '"id":"\K[^"]+')

  if [[ -z "$cart_id" || "$cart_id" == "null" ]]; then
    echo "Error: Could not retrieve cart ID."
    exit 1
  fi

  echo "Cart ID: $cart_id"
}

green_sep() {
  echo -e "\n\033[1;32m========================================\033[0m"
}

# Main script starts here

# Create User
create_user

# Get JWT token
get_jwt_token

# Example operations
create_product
green_sep
get_products
green_sep
update_product 1
green_sep
delete_product 1
green_sep
create_product
green_sep
add_to_cart 2
green_sep
add_to_cart 2
green_sep
check_cart
green_sep
reduce_cart_quantity 2
green_sep
check_cart
green_sep
cart_id=$(get_cart_id)
remove_from_cart "$cart_id"
green_sep
check_cart
green_sep
add_to_wishlist 2
green_sep
check_wishlist
green_sep
remove_from_wishlist 2
green_sep
check_wishlist


