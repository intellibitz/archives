import logging
from google.appengine.ext import db
from google.appengine.api import users, images
from util.search import BaseStatusModel, BaseSearchModel
from util.template import getDictSelectHtml

logging.info('fiteclubmodel.models.py loaded!')

IMAGETYPE_CHOICES_DICT  = {
    images.JPEG: 'jpg',
    images.PNG: 'png'
}

MATCH_CHOICES_DICT = {
    1: 'start',
    2: 'progress',
    3: 'win',
    4: 'run',
}

class LoginUser(BaseStatusModel):
    nickname = db.StringProperty(required=True)
    email = db.EmailProperty()
    password = db.StringProperty()
    token = db.StringProperty()

    def __str__(self):
        return self.nickname

    def get_skipfields(self):
        #skipped fields in json format
        return ['password', 'token', 'status']

    class Admin:
        editlist_display = ('nickname', 'email', 'password', 'token', 'status', 'created', 'modified')
        add_display = ('nickname', 'email')
        edit_display = ('nickname', 'email', 'status')
        ordering = 'nickname'

class ImageStore(BaseStatusModel):
    """Image Store"""
    image = db.BlobProperty()
    thumbWidth = db.IntegerProperty(default=45)
    thumbHeight = db.IntegerProperty(default=50)
    fileName = db.StringProperty(required=True)
    imageType = db.IntegerProperty(choices=set(IMAGETYPE_CHOICES_DICT.keys()), default=images.JPEG)

    def __str__(self):
        return self.fileName

    def display_imageType(self):
        return IMAGETYPE_CHOICES_DICT[self.imageType]

    @staticmethod
    def input_imageType(listValue):
        return getDictSelectHtml(IMAGETYPE_CHOICES_DICT, int(listValue[0]), 'imageType', 'imageType')

    def validate_instance(self, flagAdd):
        if self.thumbWidth < 0:
            raise ValueError('thumbWidth should >= 0')
        if self.thumbHeight < 0:
            raise ValueError('thumbWidth should >= 0')
        if self.image is None:
            raise ValueError('image is empty')

    def put_instance(self, flagAdd):
        if self.thumbWidth == 0:
            self.thumbWidth = 45
        if self.thumbHeight == 0:
            self.thumbHeight = 50
        self.put()

    def get_skipfields(self):
        #skipped fields in json format
        return ['image', 'status']

    class Admin:
        editlist_display = ('image', 'thumbWidth', 'thumbHeight', 'fileName', 'imagetype', 'status', 'created', 'modified')
        add_display = ('image', 'fileName', 'thumbWidth', 'thumbHeight')
        edit_display = ('image', 'fileName', 'thumbWidth', 'thumbHeight', 'status')
        ordering = 'fileName'

class Profile(BaseStatusModel):
    """profile for a fighter"""
    nickname = db.StringProperty(required=True)
    user = db.UserProperty()            #Represents a user with a Google account
    mood = db.StringProperty()
    city = db.StringProperty()
    photo = db.ReferenceProperty(ImageStore)
    points = db.IntegerProperty(default=20)
    location = db.GeoPtProperty()               #class GeoPt(lat, lon)
    
    def __str__(self):
        return self.nickname
    
    def get_skipfields(self):
        #skipped fields in json format
        return ['user', 'photo', 'status']

    def display_location(self):
        if self.location:
            return 'lat=%s, lon=%s' % (str(self.location.lat), str(self.location.lon))
        else:
            return ''
        
    class Admin:
        editlist_display = ('nickname', 'photo', 'mood', 'city', 'points', 'location', 'status', 'created', 'modified')
        add_display = ('nickname', 'mood', 'city', 'points', 'location')
        edit_display = ('nickname', 'mood', 'city', 'points', 'location', 'status')
        ordering = 'nickname'
        
class Challenge(BaseStatusModel):
    """challenge store"""
    name = db.StringProperty(required=True)
    question = db.TextProperty(required=True)
    answer = db.StringProperty(required=True)
    points = db.IntegerProperty(required=True)  #points got if win the challenge

    def __str__(self):
        return self.name
    
    def get_skipfields(self):
        #skipped fields in json format
        return ['answer', 'status']

    class Admin:
        editlist_display = ('name', 'question', 'answer', 'points', 'status', 'created', 'modified')
        add_display = ('name', 'question', 'answer', 'points')
        edit_display = ('name', 'question', 'answer', 'points', 'status')
        ordering = 'name'

class Match(BaseStatusModel):
    """match record"""
    player1 = db.ReferenceProperty(Profile, required=True, collection_name='player1')
    player2 = db.ReferenceProperty(Profile, required=True, collection_name='player2')
    challenge = db.ReferenceProperty(Challenge, required=True)  #current challenge
    state = db.IntegerProperty(choices=set(MATCH_CHOICES_DICT.keys()), default=1)
    wintarget = db.IntegerProperty(required=True)       #win rounds of this match
    winpoints = db.IntegerProperty(required=True)       #points got if a player win
    runpoints = db.IntegerProperty(required=True)       #points loss if a player run
    win1 = db.IntegerProperty(default=0)                         #win count by player1; -1 means the player run
    win2 = db.IntegerProperty(default=0)                         #win count by player2; -1 means the player run
    
    def __str__(self):
        return 'lat=%s, lon=%s' % (str(self.player1), str(self.player2))
    
    def display_state(self):
        return MATCH_CHOICES_DICT[self.state]

    @staticmethod
    def input_state(listValue):
        return getDictSelectHtml(MATCH_CHOICES_DICT, int(listValue[0]), 'state')
    
    def get_skipfields(self):
        #skipped fields in json format
        return ['status',]

    class Admin:
        editlist_display = ('player1', 'player2', 'challenge', 'state', 'wintarget', 'winpoints', 'runpoints', 'win1', 'win2', 'status', 'created', 'modified')
        add_display = ('player1', 'player2', 'challenge', 'state', 'wintarget', 'winpoints', 'runpoints', 'win1', 'win2')
        edit_display = ('player1', 'player2', 'challenge', 'state', 'wintarget', 'winpoints', 'runpoints', 'win1', 'win2', 'status')
        ordering = '-created'



