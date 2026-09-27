package com.retailwave.fce.client;
/**
 * $Id: FCEPresenter.java 5 2010-06-03 11:07:35Z muthu $
 * $HeadURL: svn://10.10.200.111:3691/Finance/tags/framework-snapshot1/fce/src/main/java/com/retailwave/fce/client/FCEPresenter.java $
 */

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.retailwave.fce.client.content.i18n.FCEConstants;
import com.retailwave.fce.client.content.user.AddUser;
import com.retailwave.fce.shared.dto.UserDTO;
import com.retailwave.fce.client.presenter.*;
import com.retailwave.fce.shared.rpc.UserServiceRemote;
import com.retailwave.fce.shared.rpc.UserServiceRemoteAsync;
import com.retailwave.fce.client.util.UIHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * FCE
 * <p>The Main UI
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class FCEPresenter implements Presenter, ValueChangeHandler<String> {
    private final HandlerManager eventBus;
    private HasWidgets container;

    static final FCEConstants myConstants = GWT.create(FCEConstants.class);

    interface FCEClientBundle extends ClientBundle {
        ImageResource users();

        ImageResource other();

        @CssResource.NotStrict
        @Source("FCE.css")
        CssResource css();
    }

    private FCEClientBundle clientBundle = GWT.create(FCEClientBundle.class);

    interface Binder extends UiBinder<Application, FCEPresenter> {
    }

    private static final Binder binder = GWT.create(Binder.class);
    @UiField
    Application application;

    /**
     * A mapping of history tokens to their associated menu items.
     */
    private Map<String, TreeItem> historyToMenus = new HashMap<String, TreeItem>();
    /**
     * A mapping of menu items to the widget display when the item is selected.
     */
    private Map<TreeItem, ContentPresenter> menuToPresenters = new HashMap<TreeItem, ContentPresenter>();

    private UserServiceRemoteAsync userServiceAsync = UserServiceRemote.App.getInstance();


    public FCEPresenter(HandlerManager eventBus) {
        this.eventBus = eventBus;
    }

    private void bind() {
        History.addValueChangeHandler(this);
        // Inject global styles.
        clientBundle.css().ensureInjected();
        binder.createAndBindUi(this);
    }

    @Override
    public void go(HasWidgets container) {
        bind();
        this.container = container;
        this.container.add(application);
        UIHelper.setApplication(application);
        application.showMessage(myConstants.startupMessage());
// kick off global rpc inits, async (so will load in parallel)
// NOTE: do this after setting up the application UI, since on failure error message will be displayed in application
// initialize data from server, to be used by the UI
//        VSCHelper.fetchAll();
// fire it right away
        loadTimer.schedule(1);
    }

    Timer loadTimer = new Timer() {
        @Override
        public void run() {
            setupMainMenu();
            setupTreeSelectionHandler();
            displayContent();
// welcome, after loading the homepage.. not before (low priority item and async)
            welcomeUser();
        }
    };

    private void welcomeUser() {
        userServiceAsync.getUser(new AsyncCallback<UserDTO>() {
            @Override
            public void onFailure(Throwable throwable) {
                application.welcome(" UserDTO!");
            }

            @Override
            public void onSuccess(UserDTO userDTO) {
                String s = userDTO.getName();
                if (null == s) {
                    s = " Guest!";
                }
                application.welcome(s);
            }
        });
    }

    int comingFromSetState = 0;
    boolean prevOpenState = true;

    private void setupTreeSelectionHandler() {
        // Add a handler that sets the content widget when a menu item is selected
        application.addSelectionHandler(new SelectionHandler<TreeItem>() {
            public void onSelection(SelectionEvent<TreeItem> event) {
                TreeItem item = event.getSelectedItem();
// open, close tree nodes
// bug fix workaround: see // http://code.google.com/p/google-web-toolkit/issues/detail?id=3660
                if (item.getChildCount() > 0) {
//                    item.setState(!item.getState());
                    if (comingFromSetState == 1 && prevOpenState) {
                        comingFromSetState++;
                    }
                    if (comingFromSetState != 2) {
                        comingFromSetState++;
                        item.setState(!item.getState());
                        prevOpenState = !item.getState();
                    } else {
                        comingFromSetState = 0;
                        prevOpenState = true;
                    }
                } else {
                    ContentPresenter contentPresenter = menuToPresenters.get(item);
                    ContentWidget historyContent = (ContentWidget) contentPresenter.getContentView();
                    if (historyContent != null) {
                        final ContentWidget currentContent = application.getContentWidget();
                        if (historyContent.equals(currentContent)) {
                            currentContent.onMenuSelection();
                        } else {
                            History.newItem(historyContent.getName());
                        }
                    }
                }
            }
        });
    }

    /**
     * Called when {@link com.google.gwt.event.logical.shared.ValueChangeEvent} is fired.
     *
     * @param stringValueChangeEvent the {@link com.google.gwt.event.logical.shared.ValueChangeEvent} that was fired
     */
    @Override
    public void onValueChange(ValueChangeEvent<String> stringValueChangeEvent) {
        // Setup a history handler to reselect the associate menu historyItem
        TreeItem historyItem = historyToMenus.get(stringValueChangeEvent.getValue());
        if (historyItem == null) {
            historyItem = application.getMainMenu().getItem(0).getChild(0);
        }
        displayContentWidget(historyItem);
    }

    private void displayContent() {
        if (History.getToken().length() > 0) {
            History.fireCurrentHistoryState();
        } else {
            // Use the first token available
            TreeItem firstItem = application.getMainMenu().getItem(0).getChild(0);
            displayContentWidget(firstItem);
        }
    }

    /**
     * Setup all of the options in the main menu.
     */
    private void setupMainMenu() {
        Tree mainMenu = application.getMainMenu();

        TreeItem users = mainMenu.addItem(myConstants.users());
        setupMainMenuOption(users, new AddUserPresenter(userServiceAsync, eventBus, new AddUser()), clientBundle.users());
//        setupMainMenuOption(users, new SearchUserPresenter(userServiceAsync, eventBus, new SearchUser()), clientBundle.users());

// todo: enable them for future versions as per roadmap
        /*

        TreeItem partners = mainMenu.addItem(myConstants.partners());
        setupMainMenuOption(partners, new AddPartnerPresenter(partnerServiceAsync, eventBus, new AddPartner()), clientBundle.partners());
        setupMainMenuOption(partners, new SearchPartnerPresenter(partnerServiceAsync, eventBus, new SearchPartner()), clientBundle.partners());

        TreeItem widgets = mainMenu.addItem(myConstants.widgets());
        setupMainMenuOption(widgets, new AddWidget(), clientBundle.widgets());
        setupMainMenuOption(widgets, new SearchWidget(), clientBundle.widgets());

        TreeItem other = mainMenu.addItem(myConstants.other());
        setupMainMenuOption(other, new Announcements(), clientBundle.other());
        setupMainMenuOption(other, new RunBot(), clientBundle.other());
        setupMainMenuOption(other, new ImportEntities(), clientBundle.other());
        */
    }

    /**
     * Add an option to the main menu.
     *
     * @param parent           the {@link com.google.gwt.user.client.ui.TreeItem} that is the option
     * @param contentPresenter the {@link com.retailwave.fce.client.ContentWidget} to display when selected
     * @param image            the icon to display next to the {@link com.google.gwt.user.client.ui.TreeItem}
     */
    private void setupMainMenuOption(TreeItem parent, ContentPresenter contentPresenter,
                                     ImageResource image) {
        // Create the TreeItem
        TreeItem treeItem = parent.addItem(AbstractImagePrototype.create(image).getHTML() + " " + contentPresenter.getName());
        mapItemContentToHistoryToken(contentPresenter, treeItem);
    }

    public void mapItemContentToHistoryToken(ContentPresenter contentPresenter, TreeItem treeItem) {
        // Map the item to its history token and content widget
//        historyToMenus.put(content.getName(), treeItem);
        String[] tabs = contentPresenter.getHistoryTokens();
        for (String tab : tabs) {
            historyToMenus.put(tab, treeItem);
        }
        menuToPresenters.put(treeItem, contentPresenter);
    }

    /**
     * Set the content to the {@link com.retailwave.fce.client.ContentWidget}.
     *
     * @param treeItem the item for which the content widget to be shown
     */
    public void displayContentWidget(TreeItem treeItem) {
        Tree tree = application.getMainMenu();
        tree.setSelectedItem(treeItem, false);
        tree.ensureSelectedItemVisible();
        ContentPresenter contentPresenter = menuToPresenters.get(treeItem);
        ContentWidget historyContent = (ContentWidget) contentPresenter.getContentView();
        if (historyContent != null) {
// remove the input objects from the global cache           
//            UIHelper.clearAllInputCache();
            final ContentWidget currentContent = application.getContentWidget();
// todo: do history required for sub tabs?? (investigate requirement)
// check if currentContent tabs differ, history needs to be maintained across tabs too
// if history differs from the selected tab, then move across tab
            if (null != currentContent && currentContent.getName().equals(History.getToken())) {
// the same widget, but tab change.. reset the widget so the widget takes care of the tab history life cycle
                application.setContentWidget(historyContent);
                historyContent.onLoad();
            } else {
                application.setContentWidget(historyContent);
            }
            contentPresenter.go(null);
        }
    }

}