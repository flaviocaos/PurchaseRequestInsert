# PurchaseRequestInsert

Este repositório contém a implementação da ação `PurchaseRequestInsert` do módulo **Requisições de Compra** de uma aplicação Java modular.

## 📋 Descrição

A classe `PurchaseRequestInsert` permite a criação de novas requisições de compra, com preenchimento automático de informações do usuário logado e validações de campos obrigatórios antes da persistência no banco de dados. O fluxo é dividido em três partes principais:

- `onWindowRequest`: Constrói a interface da tela de inserção com os campos necessários.
- `onValidationRequest`: Valida os dados preenchidos pelo usuário (campos obrigatórios, integridade de IDs).
- `onSaveRequest`: Cria a entidade `PurchaseRequest`, define atributos e persiste no banco.

## ✅ Campos incluídos na Requisição

- Empresa (Select)
- Depósito (Select)
- Status (automático: PENDING)
- Requisitante (automático: usuário atual)
- Tipo de Produto (Select)
- Produto (opcional)
- Quantidade (Input)
- Descrição (Textarea, mínimo 25 caracteres)

## ⚙️ Tecnologias utilizadas

- Java 17+
- Framework modular interno (Firsti)
- JPA (Jakarta Persistence API)
- WebSocket-based UI builder

## 📁 Estrutura esperada

```
src/
└── br/
    └── com/
        └── firsti/
            └── packages/
                └── purchase/
                    └── modules/
                        └── purchaseRequest/
                            └── actions/
                                └── PurchaseRequestInsert.java
```

## 👨‍💻 Autor

Este módulo foi desenvolvido por [Seu Nome Aqui], integrando lógica de backend e interface para o fluxo de inserção de pedidos de compra.

## 📝 Licença

Distribuído sob licença privada ou MIT, conforme o projeto principal.