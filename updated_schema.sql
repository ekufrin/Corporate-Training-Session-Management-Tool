PGDMP                      }            javafx    16.3    16.3 D    A           0    0    ENCODING    ENCODING        SET client_encoding = 'UTF8';
                      false            B           0    0 
   STDSTRINGS 
   STDSTRINGS     (   SET standard_conforming_strings = 'on';
                      false            C           0    0 
   SEARCHPATH 
   SEARCHPATH     8   SELECT pg_catalog.set_config('search_path', '', false);
                      false            D           1262    29415    javafx    DATABASE     |   CREATE DATABASE javafx WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'Croatian_Croatia.1250';
    DROP DATABASE javafx;
                postgres    false            U           1247    29417    session_status    TYPE     [   CREATE TYPE public.session_status AS ENUM (
    'SCHEDULED',
    'FINISHED',
    'FULL'
);
 !   DROP TYPE public.session_status;
       public          postgres    false            �            1259    29427    feedback    TABLE     ?  CREATE TABLE public.feedback (
    id integer NOT NULL,
    session_id integer NOT NULL,
    user_id integer NOT NULL,
    rating integer NOT NULL,
    comment text,
    "timestamp" timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT feedback_rating_check CHECK (((rating >= 1) AND (rating <= 5)))
);
    DROP TABLE public.feedback;
       public         heap    postgres    false            �            1259    29434    feedback_id_seq    SEQUENCE     �   CREATE SEQUENCE public.feedback_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 &   DROP SEQUENCE public.feedback_id_seq;
       public          postgres    false    217            E           0    0    feedback_id_seq    SEQUENCE OWNED BY     C   ALTER SEQUENCE public.feedback_id_seq OWNED BY public.feedback.id;
          public          postgres    false    218            �            1259    29435    log    TABLE     -  CREATE TABLE public.log (
    id integer NOT NULL,
    user_id integer NOT NULL,
    changed_field character varying(100) NOT NULL,
    old_value text,
    new_value text NOT NULL,
    operation character varying(255) NOT NULL,
    "timestamp" timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);
    DROP TABLE public.log;
       public         heap    postgres    false            �            1259    29441 
   log_id_seq    SEQUENCE     �   CREATE SEQUENCE public.log_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 !   DROP SEQUENCE public.log_id_seq;
       public          postgres    false    219            F           0    0 
   log_id_seq    SEQUENCE OWNED BY     9   ALTER SEQUENCE public.log_id_seq OWNED BY public.log.id;
          public          postgres    false    220            �            1259    29442    notification    TABLE     �   CREATE TABLE public.notification (
    id integer NOT NULL,
    message text NOT NULL,
    sent_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);
     DROP TABLE public.notification;
       public         heap    postgres    false            �            1259    29448    notification_id_seq    SEQUENCE     �   CREATE SEQUENCE public.notification_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 *   DROP SEQUENCE public.notification_id_seq;
       public          postgres    false    221            G           0    0    notification_id_seq    SEQUENCE OWNED BY     K   ALTER SEQUENCE public.notification_id_seq OWNED BY public.notification.id;
          public          postgres    false    222            �            1259    29454    role    TABLE     u   CREATE TABLE public.role (
    id integer NOT NULL,
    name character varying(50) NOT NULL,
    description text
);
    DROP TABLE public.role;
       public         heap    postgres    false            �            1259    29459    role_id_seq    SEQUENCE     �   CREATE SEQUENCE public.role_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 "   DROP SEQUENCE public.role_id_seq;
       public          postgres    false    223            H           0    0    role_id_seq    SEQUENCE OWNED BY     ;   ALTER SEQUENCE public.role_id_seq OWNED BY public.role.id;
          public          postgres    false    224            �            1259    29460    training_session    TABLE     �  CREATE TABLE public.training_session (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    date timestamp without time zone NOT NULL,
    duration integer NOT NULL,
    trainer_id integer NOT NULL,
    max_participants integer NOT NULL,
    location character varying(100) NOT NULL,
    status public.session_status DEFAULT 'SCHEDULED'::public.session_status NOT NULL
);
 $   DROP TABLE public.training_session;
       public         heap    postgres    false    853    853            �            1259    29464    training_session_id_seq    SEQUENCE     �   CREATE SEQUENCE public.training_session_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 .   DROP SEQUENCE public.training_session_id_seq;
       public          postgres    false    225            I           0    0    training_session_id_seq    SEQUENCE OWNED BY     S   ALTER SEQUENCE public.training_session_id_seq OWNED BY public.training_session.id;
          public          postgres    false    226            �            1259    29423    user    TABLE       CREATE TABLE public."user" (
    id integer NOT NULL,
    first_name character varying(50) NOT NULL,
    last_name character varying(50) NOT NULL,
    email character varying(100) NOT NULL,
    password_hash character varying(255) NOT NULL,
    role_id integer NOT NULL
);
    DROP TABLE public."user";
       public         heap    postgres    false            �            1259    29426    user_id_seq    SEQUENCE     �   CREATE SEQUENCE public.user_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
 "   DROP SEQUENCE public.user_id_seq;
       public          postgres    false    215            J           0    0    user_id_seq    SEQUENCE OWNED BY     =   ALTER SEQUENCE public.user_id_seq OWNED BY public."user".id;
          public          postgres    false    216            �            1259    29465    users_notifications    TABLE     �   CREATE TABLE public.users_notifications (
    user_id integer NOT NULL,
    notification_id integer NOT NULL,
    is_read boolean DEFAULT false,
    read_date timestamp without time zone
);
 '   DROP TABLE public.users_notifications;
       public         heap    postgres    false            �            1259    29469    users_sessions    TABLE     �   CREATE TABLE public.users_sessions (
    user_id integer NOT NULL,
    session_id integer NOT NULL,
    joined_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);
 "   DROP TABLE public.users_sessions;
       public         heap    postgres    false            u           2604    29474    feedback id    DEFAULT     j   ALTER TABLE ONLY public.feedback ALTER COLUMN id SET DEFAULT nextval('public.feedback_id_seq'::regclass);
 :   ALTER TABLE public.feedback ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    218    217            w           2604    29475    log id    DEFAULT     `   ALTER TABLE ONLY public.log ALTER COLUMN id SET DEFAULT nextval('public.log_id_seq'::regclass);
 5   ALTER TABLE public.log ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    220    219            y           2604    29476    notification id    DEFAULT     r   ALTER TABLE ONLY public.notification ALTER COLUMN id SET DEFAULT nextval('public.notification_id_seq'::regclass);
 >   ALTER TABLE public.notification ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    222    221            {           2604    29478    role id    DEFAULT     b   ALTER TABLE ONLY public.role ALTER COLUMN id SET DEFAULT nextval('public.role_id_seq'::regclass);
 6   ALTER TABLE public.role ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    224    223            |           2604    29479    training_session id    DEFAULT     z   ALTER TABLE ONLY public.training_session ALTER COLUMN id SET DEFAULT nextval('public.training_session_id_seq'::regclass);
 B   ALTER TABLE public.training_session ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    226    225            t           2604    29473    user id    DEFAULT     d   ALTER TABLE ONLY public."user" ALTER COLUMN id SET DEFAULT nextval('public.user_id_seq'::regclass);
 8   ALTER TABLE public."user" ALTER COLUMN id DROP DEFAULT;
       public          postgres    false    216    215            3          0    29427    feedback 
   TABLE DATA                 public          postgres    false    217   �M       5          0    29435    log 
   TABLE DATA                 public          postgres    false    219   �N       7          0    29442    notification 
   TABLE DATA                 public          postgres    false    221   2S       9          0    29454    role 
   TABLE DATA                 public          postgres    false    223   nT       ;          0    29460    training_session 
   TABLE DATA                 public          postgres    false    225   �T       1          0    29423    user 
   TABLE DATA                 public          postgres    false    215   �U       =          0    29465    users_notifications 
   TABLE DATA                 public          postgres    false    227   JW       >          0    29469    users_sessions 
   TABLE DATA                 public          postgres    false    228   \X       K           0    0    feedback_id_seq    SEQUENCE SET     =   SELECT pg_catalog.setval('public.feedback_id_seq', 4, true);
          public          postgres    false    218            L           0    0 
   log_id_seq    SEQUENCE SET     9   SELECT pg_catalog.setval('public.log_id_seq', 52, true);
          public          postgres    false    220            M           0    0    notification_id_seq    SEQUENCE SET     B   SELECT pg_catalog.setval('public.notification_id_seq', 10, true);
          public          postgres    false    222            N           0    0    role_id_seq    SEQUENCE SET     9   SELECT pg_catalog.setval('public.role_id_seq', 2, true);
          public          postgres    false    224            O           0    0    training_session_id_seq    SEQUENCE SET     F   SELECT pg_catalog.setval('public.training_session_id_seq', 12, true);
          public          postgres    false    226            P           0    0    user_id_seq    SEQUENCE SET     :   SELECT pg_catalog.setval('public.user_id_seq', 13, true);
          public          postgres    false    216            �           2606    29485    feedback feedback_pkey 
   CONSTRAINT     T   ALTER TABLE ONLY public.feedback
    ADD CONSTRAINT feedback_pkey PRIMARY KEY (id);
 @   ALTER TABLE ONLY public.feedback DROP CONSTRAINT feedback_pkey;
       public            postgres    false    217            �           2606    29487    log log_pkey 
   CONSTRAINT     J   ALTER TABLE ONLY public.log
    ADD CONSTRAINT log_pkey PRIMARY KEY (id);
 6   ALTER TABLE ONLY public.log DROP CONSTRAINT log_pkey;
       public            postgres    false    219            �           2606    29489    notification notification_pkey 
   CONSTRAINT     \   ALTER TABLE ONLY public.notification
    ADD CONSTRAINT notification_pkey PRIMARY KEY (id);
 H   ALTER TABLE ONLY public.notification DROP CONSTRAINT notification_pkey;
       public            postgres    false    221            �           2606    29495    role role_name_key 
   CONSTRAINT     M   ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_name_key UNIQUE (name);
 <   ALTER TABLE ONLY public.role DROP CONSTRAINT role_name_key;
       public            postgres    false    223            �           2606    29497    role role_pkey 
   CONSTRAINT     L   ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_pkey PRIMARY KEY (id);
 8   ALTER TABLE ONLY public.role DROP CONSTRAINT role_pkey;
       public            postgres    false    223            �           2606    29499 &   training_session training_session_pkey 
   CONSTRAINT     d   ALTER TABLE ONLY public.training_session
    ADD CONSTRAINT training_session_pkey PRIMARY KEY (id);
 P   ALTER TABLE ONLY public.training_session DROP CONSTRAINT training_session_pkey;
       public            postgres    false    225            �           2606    29481    user user_email_key 
   CONSTRAINT     Q   ALTER TABLE ONLY public."user"
    ADD CONSTRAINT user_email_key UNIQUE (email);
 ?   ALTER TABLE ONLY public."user" DROP CONSTRAINT user_email_key;
       public            postgres    false    215            �           2606    29483    user user_pkey 
   CONSTRAINT     N   ALTER TABLE ONLY public."user"
    ADD CONSTRAINT user_pkey PRIMARY KEY (id);
 :   ALTER TABLE ONLY public."user" DROP CONSTRAINT user_pkey;
       public            postgres    false    215            �           2606    29501 ,   users_notifications users_notifications_pkey 
   CONSTRAINT     �   ALTER TABLE ONLY public.users_notifications
    ADD CONSTRAINT users_notifications_pkey PRIMARY KEY (user_id, notification_id);
 V   ALTER TABLE ONLY public.users_notifications DROP CONSTRAINT users_notifications_pkey;
       public            postgres    false    227    227            �           2606    29503 "   users_sessions users_sessions_pkey 
   CONSTRAINT     q   ALTER TABLE ONLY public.users_sessions
    ADD CONSTRAINT users_sessions_pkey PRIMARY KEY (user_id, session_id);
 L   ALTER TABLE ONLY public.users_sessions DROP CONSTRAINT users_sessions_pkey;
       public            postgres    false    228    228            �           1259    29505    idx_feedback_rating    INDEX     J   CREATE INDEX idx_feedback_rating ON public.feedback USING btree (rating);
 '   DROP INDEX public.idx_feedback_rating;
       public            postgres    false    217            �           1259    29506    idx_log_user    INDEX     _   CREATE INDEX idx_log_user ON public.log USING btree (user_id) WITH (deduplicate_items='true');
     DROP INDEX public.idx_log_user;
       public            postgres    false    219            �           1259    29507    idx_session_date    INDEX     M   CREATE INDEX idx_session_date ON public.training_session USING btree (date);
 $   DROP INDEX public.idx_session_date;
       public            postgres    false    225            �           1259    29504    idx_user_role    INDEX     C   CREATE INDEX idx_user_role ON public."user" USING btree (role_id);
 !   DROP INDEX public.idx_user_role;
       public            postgres    false    215            �           2606    29518 !   feedback feedback_session_id_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.feedback
    ADD CONSTRAINT feedback_session_id_fkey FOREIGN KEY (session_id) REFERENCES public.training_session(id) ON DELETE CASCADE;
 K   ALTER TABLE ONLY public.feedback DROP CONSTRAINT feedback_session_id_fkey;
       public          postgres    false    217    4756    225            �           2606    29513    feedback feedback_user_id_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.feedback
    ADD CONSTRAINT feedback_user_id_fkey FOREIGN KEY (user_id) REFERENCES public."user"(id) ON DELETE CASCADE;
 H   ALTER TABLE ONLY public.feedback DROP CONSTRAINT feedback_user_id_fkey;
       public          postgres    false    4741    215    217            �           2606    29523    log log_user_id_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.log
    ADD CONSTRAINT log_user_id_fkey FOREIGN KEY (user_id) REFERENCES public."user"(id) ON DELETE CASCADE;
 >   ALTER TABLE ONLY public.log DROP CONSTRAINT log_user_id_fkey;
       public          postgres    false    215    4741    219            �           2606    29538 1   training_session training_session_trainer_id_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.training_session
    ADD CONSTRAINT training_session_trainer_id_fkey FOREIGN KEY (trainer_id) REFERENCES public."user"(id) ON DELETE CASCADE;
 [   ALTER TABLE ONLY public.training_session DROP CONSTRAINT training_session_trainer_id_fkey;
       public          postgres    false    225    4741    215            �           2606    29508    user user_role_id_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public."user"
    ADD CONSTRAINT user_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.role(id) ON DELETE CASCADE;
 B   ALTER TABLE ONLY public."user" DROP CONSTRAINT user_role_id_fkey;
       public          postgres    false    4753    215    223            �           2606    29543 <   users_notifications users_notifications_notification_id_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.users_notifications
    ADD CONSTRAINT users_notifications_notification_id_fkey FOREIGN KEY (notification_id) REFERENCES public.notification(id) ON DELETE CASCADE;
 f   ALTER TABLE ONLY public.users_notifications DROP CONSTRAINT users_notifications_notification_id_fkey;
       public          postgres    false    221    4749    227            �           2606    29548 4   users_notifications users_notifications_user_id_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.users_notifications
    ADD CONSTRAINT users_notifications_user_id_fkey FOREIGN KEY (user_id) REFERENCES public."user"(id) ON DELETE CASCADE;
 ^   ALTER TABLE ONLY public.users_notifications DROP CONSTRAINT users_notifications_user_id_fkey;
       public          postgres    false    215    4741    227            �           2606    29558 -   users_sessions users_sessions_session_id_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.users_sessions
    ADD CONSTRAINT users_sessions_session_id_fkey FOREIGN KEY (session_id) REFERENCES public.training_session(id) ON DELETE CASCADE;
 W   ALTER TABLE ONLY public.users_sessions DROP CONSTRAINT users_sessions_session_id_fkey;
       public          postgres    false    228    225    4756            �           2606    29553 *   users_sessions users_sessions_user_id_fkey    FK CONSTRAINT     �   ALTER TABLE ONLY public.users_sessions
    ADD CONSTRAINT users_sessions_user_id_fkey FOREIGN KEY (user_id) REFERENCES public."user"(id) ON DELETE CASCADE;
 T   ALTER TABLE ONLY public.users_sessions DROP CONSTRAINT users_sessions_user_id_fkey;
       public          postgres    false    215    228    4741            3   �   x���=n�@��S��D�hY��'�H�-�N�6��Ev��R����+ڤp3z�b��)v�����G?����7�뒪�����wQ�8@�L�֝�~�m��a�Z���f�����ߚ\����	L���S�RH�r	��(I(�4�k���+n�#��>.��Ѣ�`=O�<i�Ԡt�l��"�mG��2�:J���vn�����`�һ�_%zȕ
��e��*���ck      5   4  x����N#G������muU���D�&-�r3��� ���V9%�j�ؘ�6E/�%f�������]���?��O�����]3�w퍺<<��p����Ԁ5���:�8=���b4�g���� ��߃Q�*�*g5%J?���x?;�9��i'����ܨ�vr������~<��㌼�.լ[�b����d�� ��J�e��!�`l9�y( � �֘v1��閾��8�M2 �b�?���P�Q�	�|��=�m&]>tW�tx�D��h���J��;Ч��2|��{�ް�ۡd�%�(��TV�\�^�����4�HI��Of�N䃗d,�:����X���E+�XSbe�6�H��E%f�& Z	ǕrR�qcQq|!�Q�IG
�8	'�rB��R�"wb9�.�hdaK��T�S$Z�������7�$i�e�sX�A#o%��XxX2�5��$R��w�\$'��}���8�|��P��l�H�78-�
��zI�;���k9KE��7&?���j8�U��1����`�����?��p�+ŐL?\~RQ��+��D�&>��ӱ���H^;t���g�Ԇ\)((c*��:�����bV��?���ô��������qd���Վ<� 	%��G�g�����]�DG��ցhe��{��H��_���V}�o�K��߾���DQ0l�<�IA�E�'�7+� �h�,ho�élr�f��*RA{�q�;)ފ%n&�I�y���Ѽ�v���v_��X��γK�HO��)����ꇮH� ��N��
�N#���|J�`�^���ys݌X�s�^�w�m���z���a�f��v�Pu��?�7��J��f���r�h�滶>�X���Rӣ�`E?P@a�-IyƵъ1�*�G���\��P@ISb�}븹�[�Z�(���K���$�e�.'_1˯��hy ����F���Q{';�m��;�~ }���e�bF�]�q�_]F��|�N"{6'�x�3  �M�|�O������s�ߚ�4gA����������J����zA�}��&�厾{�?��V8      7   ,  x���=O�0ཿ�
�|�9f*R�"Tؒ��i\��}��Vj���:Y~^��\�o,W����[ֹ`?���u��?�/�p��0]����a�X�{ۅx�*=T�tPֵ�o���B�8�D
�k�h�GI
�w��?IqB�ڶ�_1�Ji��TerH'`a:{�QTZ�$#"��1�:���L�ʶ�b
�Qj����d����O�il�\�TR�`�R2���l�Q;��$�WI��,%D��!�ǯ6��;�w1�݃�+z�a>�l��b���ݻ�o���xX�Tb�0��)��      9   N   x���v
Q���W((M��L�+��IUs�	uV�0�QP)J��K-RGbjZsy�iT�[��_�������� ��#�      ;   �   x������0 �{�bnuA�$Z���jŠT1Ճ�k�aK"��?�_��Ĝ�m2�<f�3��s�Y����P���K%�Ѧ�ת��5��ZlR�^�c*�#,��!K���.iC��dQ��+��Y:	?���^��m�QFdX0/��}H���1�͕��K�����W�W1����*U��R��
3p��S
$N�������ޞ�c$7��]      1   z  x����n�@��>1&�I3��b�Q�?X�w�GG��[uվWc��iRW�ޓܜ/�(�bd�����Q'	]P�{�T��`n�ܝ��Օ��1A9	?����t�'���x��Z��VM-�'h �>q�@�N�)�����e�^K0�bG��/�j�5�G����+ʿHbi�H|�2��l�;����/&M�� ��$ؤ�^�4Nz��y�t:$��&ͥ<,�]��5�t��5)�[71�K�ޤ�P��_��R�;� ������3=��ӤK=��bI�]�� 9�x,�e84��XLn�XB�5�a�>߽��ܖ��IйK_�# ��賞a)J����W�61�mهO�xR��|����>�V��U*_Hb�F      =     x����jA������:$�dvfz����b�j�bEaA�������ܶб�����O��b���z�|�����n������x��}��������s��-�AJ���+a��:E�rl��(�� ����L�2'8���i��z�d���,�A�H��ߑ�6H��	�V!g��Ft�X�4ϙ�d�h)�3��y�GN��͡K���i�b��\��V��&Fuyr�~�0�#�T��T��*J�m�i'�� ���H��@���������7�      >   �   x��ѽ
�@�>O�]̱?��������F[AI!����o�&hc`�a�Y���u����t���+w�|�]Η�-�a��/w0�3�S2�V�P0����P���h����!���=q�AEF{���&)��Tjͣ�:V	q$ח�!���D��KBQ�i$�� �)9�>_���~�     