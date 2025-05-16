package br.com.firsti.packages.purchase.modules.purchaseRequest.actions;

import java.math.BigDecimal;
import java.util.Date;

import br.com.firsti.languages.LRCore;
import br.com.firsti.languages.LRProduct;
import br.com.firsti.languages.LRPurchase;
import br.com.firsti.module.actions.AbstractActionInsert;
import br.com.firsti.module.exceptions.AccessDeniedException;
import br.com.firsti.module.exceptions.ActionErrorException;
import br.com.firsti.module.exceptions.InternalServerErrorException;
import br.com.firsti.module.exceptions.PermissionDeniedException;
import br.com.firsti.module.exceptions.ResourceNotFoundException;
import br.com.firsti.module.requests.ActionRequest;
import br.com.firsti.module.structures.ActionBackgroundTask.ActionTaskBuilder;
import br.com.firsti.module.structures.ActionValidation.ActionValidationBuilder;
import br.com.firsti.packages.organization.entities.Collaborator;
import br.com.firsti.packages.organization.entities.Company;
import br.com.firsti.packages.product.entities.Product;
import br.com.firsti.packages.product.entities.ProductType;
import br.com.firsti.packages.purchase.entities.PurchaseRequest;
import br.com.firsti.packages.purchase.entities.PurchaseRequest.PurchaseRequestStatus;
import br.com.firsti.packages.purchase.modules.purchaseRequest.ModulePurchaseRequest;
import br.com.firsti.packages.stock.entities.Warehouse;
import br.com.firsti.persistence.EntityManagerWrapper;
import br.com.firsti.services.websocket.messages.output.elements.items.InputView;
import br.com.firsti.services.websocket.messages.output.elements.items.Select;

public class PurchaseRequestInsert extends AbstractActionInsert<ModulePurchaseRequest> {

    public PurchaseRequestInsert() {
        super(new Builder<ModulePurchaseRequest>(Access.COMPANY_PRIVATE, PurchaseRequestView.class));
    }

    @Override
    public void onWindowRequest(EntityManagerWrapper entityManager, ActionRequest request, WindowBuilder windowBuilder)
            throws AccessDeniedException, PermissionDeniedException, InternalServerErrorException,
            ResourceNotFoundException {

        Company userCompany = request.getUserProfile().getCompany();

        windowBuilder.getDataBuilder()
                .add("company", userCompany)
                .add("status", PurchaseRequestStatus.PENDING)
                .add("requester", request.getUserProfile().getEnterpriseName());

        windowBuilder.getHeaderBuilder()
                .add(new Select("company").setLabel(LRCore.COMPANY).addClass("col-4"))
                .add(new Select("warehouse").setLabel(LRCore.WAREHOUSE).addClass("col-6"))
                .add(new InputView("status").setLabel(LRCore.STATUS).setTranslate(LRPurchase.class).addClass("col-2"));

        windowBuilder.getBodyBuilder()
                .add(new InputView("requester").setLabel(LRCore.REQUESTER).addClass("col-4"))
                .add(new InputView("creation").setLabel(LRCore.CREATION).addClass("col-6"))
                .add(new InputView("productType").setLabel(LRProduct.PRODUCT_TYPE).addClass("col-2"))
                .add(new InputView("productName").setLabel(LRProduct.PRODUCT).addClass("col-4"))
                .add(new InputView("quantity").setLabel(LRCore.QUANTITY).addClass("col-6"))
                .add(new InputView("description").setLabel(LRCore.DESCRIPTION).addClass("col-2"));
    }

    @Override
    public void onValidationRequest(EntityManagerWrapper entityManager, ActionRequest request,
                                    ActionValidationBuilder validationBuilder)
            throws AccessDeniedException, ResourceNotFoundException, ActionErrorException, InternalServerErrorException {

        if (request.isEmpty("company")) {
            validationBuilder.addCannotBeEmpty("company");
        } else {
            Company company = entityManager.find(Company.class, request.getInteger("company"));
            if (company == null) {
                validationBuilder.addInvalidValue("company");
            } else {
                request.set("company", company);
            }
        }

        if (request.isEmpty("warehouse")) {
            validationBuilder.addCannotBeEmpty("warehouse");
        } else {
            Warehouse warehouse = entityManager.find(Warehouse.class, request.getInteger("warehouse"));
            if (warehouse == null) {
                validationBuilder.addInvalidValue("warehouse");
            } else {
                request.set("warehouse", warehouse);
            }
        }

        if (request.isEmpty("category")) {
            validationBuilder.addCannotBeEmpty("category");
        }

        ProductType type = null;
        if (request.isEmpty("productType")) {
            validationBuilder.addCannotBeEmpty("productType");
        } else {
            type = entityManager.find(ProductType.class, request.getInteger("productType"));
            if (type == null) {
                validationBuilder.addInvalidValue("productType");
            } else {
                request.set("productType", type);
            }
        }

        if (!request.isEmpty("product")) {
            Product product = entityManager.find(Product.class, request.getInteger("product"));
            if (product == null) {
                validationBuilder.addInvalidValue("product");
            } else {
                request.set("product", product);
            }
        }

        if (request.isEmpty("quantity")) {
            validationBuilder.addCannotBeEmpty("quantity");
        }

        String description = request.getString("description");
        if (description == null || description.trim().length() < 25) {
            validationBuilder.addMinimumLength("description", 25);
        }
    }

    @Override
    public Object onSaveRequest(EntityManagerWrapper entityManager, ActionRequest request,
                                ActionTaskBuilder taskBuilder)
            throws ResourceNotFoundException, ActionErrorException, InternalServerErrorException {

        Warehouse warehouse = request.get("warehouse", Warehouse.class);
        ProductType productType = request.get("productType", ProductType.class);
        Product product = request.isSet("product") ? request.get("product", Product.class) : null;
        String description = request.getString("description");
        Collaborator user = request.getUserProfile().getCollaborator();

        String unit = productType != null && productType.getUnit() != null ? productType.getUnit().name() : null;
        BigDecimal quantity;
        if ("UNIT".equalsIgnoreCase(unit)) {
            Integer qtdInt = request.get("quantity", Integer.class);
            quantity = BigDecimal.valueOf(qtdInt);
        } else {
            Double qtdDouble = request.get("quantity", Double.class);
            quantity = BigDecimal.valueOf(qtdDouble);
        }

        PurchaseRequest entity = new PurchaseRequest(warehouse, productType, product, description, quantity);
        entity.setStatus(PurchaseRequestStatus.PENDING);
        entity.setRequester(user);
        entity.setCreation(new Date());

        entityManager.persist(entity);

        return entity.getId();
    }
}