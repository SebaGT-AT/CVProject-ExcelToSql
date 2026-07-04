# Formatos de Importacion

Los encabezados deben respetar exactamente estos nombres en CSV o XLSX.

## PRODUCT

```text
sku,name,price,active
```

## CUSTOMER

```text
customer_code,full_name,email,phone,registration_date
```

## SUPPLIER

```text
supplier_code,company_name,email,phone
```

## EMPLOYEE

```text
employee_code,full_name,email,phone,hire_date
```

## INVENTORY

```text
product_sku,quantity,warehouse_location,last_updated
```

## Reglas de formato

- fechas: `yyyy-MM-dd`
- booleanos: `true` o `false`
- numeros decimales: usar punto decimal
- telefonos: solo digitos, espacios, parentesis, guiones y prefijo `+`
